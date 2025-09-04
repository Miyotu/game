const Daily = require('../models/daily');
const Inventory = require('../models/inventory');
const GameItems = require('../models/gameItems');
const GameLimits = require('../models/gameLimits');
const MiningSkills = require('../models/miningSkills');

class GameUtils {
  // Kullanıcının coin'ini güncelle
  static async updateUserCoins(userId, amount) {
    try {
      const user = await Daily.findOneAndUpdate(
        { userId },
        { $inc: { coin: amount } },
        { upsert: true, new: true }
      );
      return user.coin;
    } catch (error) {
      console.error('Coin güncelleme hatası:', error);
      return null;
    }
  }

  // Kullanıcının XP'sini güncelle
  static async updateUserXP(userId, amount) {
    try {
      const user = await Daily.findOneAndUpdate(
        { userId },
        { $inc: { xp: amount } },
        { upsert: true, new: true }
      );
      return user.xp;
    } catch (error) {
      console.error('XP güncelleme hatası:', error);
      return null;
    }
  }

  // Kullanıcının coin'ini kontrol et
  static async getUserCoins(userId) {
    try {
      const user = await Daily.findOne({ userId });
      return user ? user.coin : 0;
    } catch (error) {
      console.error('Coin kontrol hatası:', error);
      return 0;
    }
  }

  // Rastgele eşya ver
  static async giveRandomItem(userId, gameSource) {
    try {
      const availableItems = await GameItems.find({ gameSource });
      if (availableItems.length === 0) return null;

      // Rastgele eşya seç (drop rate'e göre)
      const randomNum = Math.random() * 100;
      let cumulativeRate = 0;
      let selectedItem = null;

      for (const item of availableItems) {
        cumulativeRate += item.dropRate;
        if (randomNum <= cumulativeRate) {
          selectedItem = item;
          break;
        }
      }

      if (!selectedItem) {
        selectedItem = availableItems[availableItems.length - 1]; // En son eşyayı ver
      }

      // Envanteri güncelle
      await this.addItemToInventory(userId, selectedItem);
      return selectedItem;
    } catch (error) {
      console.error('Eşya verme hatası:', error);
      return null;
    }
  }

  // Envantera eşya ekle
  static async addItemToInventory(userId, item) {
    try {
      let inventory = await Inventory.findOne({ userId });
      
      if (!inventory) {
        inventory = new Inventory({ userId, items: [] });
      }

      // Aynı eşyadan var mı kontrol et
      const existingItem = inventory.items.find(invItem => invItem.itemId === item.itemId);
      
      if (existingItem) {
        existingItem.quantity += 1;
      } else {
        inventory.items.push({
          itemId: item.itemId,
          name: item.name,
          rarity: item.rarity,
          quantity: 1,
          obtainedAt: new Date()
        });
      }

      // Toplam değeri güncelle
      inventory.totalValue = inventory.items.reduce((total, invItem) => {
        const itemData = item.itemId === invItem.itemId ? item : null;
        return total + (itemData ? itemData.value * invItem.quantity : 0);
      }, 0);

      inventory.lastUpdated = new Date();
      await inventory.save();
      
      return inventory;
    } catch (error) {
      console.error('Envanter güncelleme hatası:', error);
      return null;
    }
  }

  // Oyun limitlerini kontrol et
  static async checkGameLimit(userId, gameType) {
    try {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      
      let limits = await GameLimits.findOne({ userId });
      if (!limits) {
        limits = new GameLimits({ userId });
        await limits.save();
      }

      const gameLimit = limits[gameType];
      
      switch (gameType) {
        case 'dailyReward':
          const lastClaimed = gameLimit.lastClaimed;
          if (lastClaimed) {
            const lastClaimedDate = new Date(lastClaimed);
            lastClaimedDate.setHours(0, 0, 0, 0);
            return lastClaimedDate.getTime() !== today.getTime();
          }
          return true;

        case 'rockPaperScissors':
          const lastRpsDate = gameLimit.lastPlayDate;
          if (lastRpsDate) {
            const lastRpsDateOnly = new Date(lastRpsDate);
            lastRpsDateOnly.setHours(0, 0, 0, 0);
            if (lastRpsDateOnly.getTime() === today.getTime()) {
              return gameLimit.playsToday < gameLimit.maxPlaysPerDay;
            }
          }
          return true;

        case 'passwordGame':
          const lastPasswordDate = gameLimit.lastPlayDate;
          if (lastPasswordDate) {
            const lastPasswordDateOnly = new Date(lastPasswordDate);
            lastPasswordDateOnly.setHours(0, 0, 0, 0);
            if (lastPasswordDateOnly.getTime() === today.getTime()) {
              return gameLimit.playsToday < gameLimit.maxPlaysPerDay;
            }
          }
          return true;

        case 'mining':
          const lastMineTime = gameLimit.lastMineTime;
          if (lastMineTime) {
            const timeDiff = Date.now() - new Date(lastMineTime).getTime();
            const cooldownMs = gameLimit.cooldownMinutes * 60 * 1000;
            return timeDiff >= cooldownMs;
          }
          return true;

        case 'flappyBird':
          const lastFlappyDate = gameLimit.lastPlayDate;
          if (lastFlappyDate) {
            const lastFlappyDateOnly = new Date(lastFlappyDate);
            lastFlappyDateOnly.setHours(0, 0, 0, 0);
            if (lastFlappyDateOnly.getTime() === today.getTime()) {
              return gameLimit.playsToday < gameLimit.maxPlaysPerDay;
            }
          }
          return true;

        default:
          return true;
      }
    } catch (error) {
      console.error('Oyun limiti kontrol hatası:', error);
      return false;
    }
  }

  // Oyun limitini güncelle
  static async updateGameLimit(userId, gameType) {
    try {
      const today = new Date();
      const updateData = {};

      switch (gameType) {
        case 'dailyReward':
          updateData['dailyReward.lastClaimed'] = new Date();
          updateData['dailyReward.canClaim'] = false;
          break;

        case 'rockPaperScissors':
          const rpsLimits = await GameLimits.findOne({ userId });
          const lastRpsDate = rpsLimits?.rockPaperScissors?.lastPlayDate;
          const todayStart = new Date(today);
          todayStart.setHours(0, 0, 0, 0);
          
          if (lastRpsDate && new Date(lastRpsDate).setHours(0, 0, 0, 0) === todayStart.getTime()) {
            updateData['rockPaperScissors.playsToday'] = (rpsLimits.rockPaperScissors.playsToday || 0) + 1;
          } else {
            updateData['rockPaperScissors.playsToday'] = 1;
          }
          updateData['rockPaperScissors.lastPlayDate'] = new Date();
          break;

        case 'passwordGame':
          const passwordLimits = await GameLimits.findOne({ userId });
          const lastPasswordDate = passwordLimits?.passwordGame?.lastPlayDate;
          const todayStartPassword = new Date(today);
          todayStartPassword.setHours(0, 0, 0, 0);
          
          if (lastPasswordDate && new Date(lastPasswordDate).setHours(0, 0, 0, 0) === todayStartPassword.getTime()) {
            updateData['passwordGame.playsToday'] = (passwordLimits.passwordGame.playsToday || 0) + 1;
          } else {
            updateData['passwordGame.playsToday'] = 1;
          }
          updateData['passwordGame.lastPlayDate'] = new Date();
          break;

        case 'mining':
          updateData['mining.lastMineTime'] = new Date();
          break;

        case 'flappyBird':
          const flappyLimits = await GameLimits.findOne({ userId });
          const lastFlappyDate = flappyLimits?.flappyBird?.lastPlayDate;
          const todayStartFlappy = new Date(today);
          todayStartFlappy.setHours(0, 0, 0, 0);
          
          if (lastFlappyDate && new Date(lastFlappyDate).setHours(0, 0, 0, 0) === todayStartFlappy.getTime()) {
            updateData['flappyBird.playsToday'] = (flappyLimits.flappyBird.playsToday || 0) + 1;
          } else {
            updateData['flappyBird.playsToday'] = 1;
          }
          updateData['flappyBird.lastPlayDate'] = new Date();
          break;
      }

      await GameLimits.findOneAndUpdate(
        { userId },
        { $set: updateData },
        { upsert: true, new: true }
      );
    } catch (error) {
      console.error('Oyun limiti güncelleme hatası:', error);
    }
  }

  // Madencilik becerilerini kontrol et
  static async getMiningSkills(userId) {
    try {
      let skills = await MiningSkills.findOne({ userId });
      if (!skills) {
        skills = new MiningSkills({ userId });
        await skills.save();
      }
      return skills;
    } catch (error) {
      console.error('Madencilik becerileri kontrol hatası:', error);
      return null;
    }
  }

  // Madencilik becerisi satın al
  static async purchaseMiningSkill(userId, skillType, cost) {
    try {
      const userCoins = await this.getUserCoins(userId);
      if (userCoins < cost) {
        return { success: false, message: 'Yetersiz coin!' };
      }

      const skills = await MiningSkills.findOne({ userId });
      if (skills && skills[skillType]) {
        return { success: false, message: 'Bu beceri zaten satın alınmış!' };
      }

      // Coin'i düş
      await this.updateUserCoins(userId, -cost);

      // Beceriyi aktif et
      const updateData = {};
      updateData[skillType] = true;
      updateData[`purchasedAt.${skillType}`] = new Date();
      updateData['totalSpent'] = (skills?.totalSpent || 0) + cost;

      await MiningSkills.findOneAndUpdate(
        { userId },
        { $set: updateData },
        { upsert: true, new: true }
      );

      return { success: true, message: 'Beceri başarıyla satın alındı!' };
    } catch (error) {
      console.error('Beceri satın alma hatası:', error);
      return { success: false, message: 'Bir hata oluştu!' };
    }
  }
}

module.exports = GameUtils;