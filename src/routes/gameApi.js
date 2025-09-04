const express = require('express');
const router = express.Router();
const GameUtils = require('../utils/gameUtils');
const GameLimits = require('../models/gameLimits');
const MiningSkills = require('../models/miningSkills');

// Günlük ödül al
router.post('/daily-reward', async (req, res) => {
  if (!req.user) {
    return res.status(401).json({ success: false, message: 'Giriş yapmalısınız!' });
  }

  try {
    const canClaim = await GameUtils.checkGameLimit(req.user.id, 'dailyReward');
    if (!canClaim) {
      return res.json({ success: false, message: 'Günlük ödülünüzü zaten aldınız!' });
    }

    // Rastgele coin miktarı (500-2000)
    const coinReward = Math.floor(Math.random() * 1500) + 500;
    const xpReward = Math.floor(Math.random() * 100) + 50;

    await GameUtils.updateUserCoins(req.user.id, coinReward);
    await GameUtils.updateUserXP(req.user.id, xpReward);
    await GameUtils.updateGameLimit(req.user.id, 'dailyReward');

    // Rastgele eşya ver (%70 şans)
    let item = null;
    if (Math.random() < 0.7) {
      item = await GameUtils.giveRandomItem(req.user.id, 'daily');
    }

    res.json({
      success: true,
      coinReward,
      xpReward,
      item: item ? { name: item.name, icon: item.icon, rarity: item.rarity } : null
    });
  } catch (error) {
    console.error('Günlük ödül hatası:', error);
    res.status(500).json({ success: false, message: 'Bir hata oluştu!' });
  }
});

// Taş kağıt makas oyna
router.post('/play-rps', async (req, res) => {
  if (!req.user) {
    return res.status(401).json({ success: false, message: 'Giriş yapmalısınız!' });
  }

  const { playerChoice } = req.body;
  const gameCost = 1000;

  try {
    const userCoins = await GameUtils.getUserCoins(req.user.id);
    if (userCoins < gameCost) {
      return res.json({ success: false, message: 'Yetersiz coin!' });
    }

    const canPlay = await GameUtils.checkGameLimit(req.user.id, 'rockPaperScissors');
    if (!canPlay) {
      return res.json({ success: false, message: 'Günlük oyun hakkınız doldu!' });
    }

    // Coin'i düş
    await GameUtils.updateUserCoins(req.user.id, -gameCost);

    const choices = ['rock', 'paper', 'scissors'];
    const botChoice = choices[Math.floor(Math.random() * choices.length)];
    
    let result = 'draw';
    let coinReward = 0;
    let xpReward = 10;

    if (playerChoice === botChoice) {
      result = 'draw';
      coinReward = gameCost; // Para iade
    } else if (
      (playerChoice === 'rock' && botChoice === 'scissors') ||
      (playerChoice === 'paper' && botChoice === 'rock') ||
      (playerChoice === 'scissors' && botChoice === 'paper')
    ) {
      result = 'win';
      coinReward = gameCost * 2; // 2x kazanç
      xpReward = 50;
    } else {
      result = 'lose';
      coinReward = 0;
      xpReward = 5;
    }

    if (coinReward > 0) {
      await GameUtils.updateUserCoins(req.user.id, coinReward);
    }
    await GameUtils.updateUserXP(req.user.id, xpReward);
    await GameUtils.updateGameLimit(req.user.id, 'rockPaperScissors');

    // Kazanırsa eşya ver (%30 şans)
    let item = null;
    if (result === 'win' && Math.random() < 0.3) {
      item = await GameUtils.giveRandomItem(req.user.id, 'rps');
    }

    res.json({
      success: true,
      result,
      playerChoice,
      botChoice,
      coinReward,
      xpReward,
      item: item ? { name: item.name, icon: item.icon, rarity: item.rarity } : null
    });
  } catch (error) {
    console.error('RPS oyun hatası:', error);
    res.status(500).json({ success: false, message: 'Bir hata oluştu!' });
  }
});

// Madencilik oyna
router.post('/play-mining', async (req, res) => {
  if (!req.user) {
    return res.status(401).json({ success: false, message: 'Giriş yapmalısınız!' });
  }

  try {
    const canMine = await GameUtils.checkGameLimit(req.user.id, 'mining');
    if (!canMine) {
      return res.json({ success: false, message: 'Madencilik cooldown\'unda!' });
    }

    const skills = await GameUtils.getMiningSkills(req.user.id);
    
    // Beceriler etkisini hesapla
    let baseReward = Math.floor(Math.random() * 200) + 100;
    let xpReward = Math.floor(Math.random() * 30) + 20;
    
    if (skills.x2Miner) {
      baseReward *= 2;
      xpReward *= 2;
    }

    await GameUtils.updateUserCoins(req.user.id, baseReward);
    await GameUtils.updateUserXP(req.user.id, xpReward);
    await GameUtils.updateGameLimit(req.user.id, 'mining');

    // Eşya drop şansı (beceriler etkisi)
    let dropChance = 0.4;
    if (skills.luckBoost) dropChance += 0.2;

    let item = null;
    if (Math.random() < dropChance) {
      item = await GameUtils.giveRandomItem(req.user.id, 'mining');
    }

    res.json({
      success: true,
      coinReward: baseReward,
      xpReward,
      item: item ? { name: item.name, icon: item.icon, rarity: item.rarity } : null,
      skills: {
        fasterMining: skills.fasterMining,
        luckBoost: skills.luckBoost,
        x2Miner: skills.x2Miner
      }
    });
  } catch (error) {
    console.error('Madencilik hatası:', error);
    res.status(500).json({ success: false, message: 'Bir hata oluştu!' });
  }
});

// Madencilik becerisi satın al
router.post('/buy-mining-skill', async (req, res) => {
  if (!req.user) {
    return res.status(401).json({ success: false, message: 'Giriş yapmalısınız!' });
  }

  const { skillType } = req.body;
  const skillCosts = {
    fasterMining: 10000,
    luckBoost: 20000,
    x2Miner: 30000
  };

  const cost = skillCosts[skillType];
  if (!cost) {
    return res.status(400).json({ success: false, message: 'Geçersiz beceri!' });
  }

  try {
    const result = await GameUtils.purchaseMiningSkill(req.user.id, skillType, cost);
    res.json(result);
  } catch (error) {
    console.error('Beceri satın alma hatası:', error);
    res.status(500).json({ success: false, message: 'Bir hata oluştu!' });
  }
});

// Uçan kuş oyna
router.post('/play-flappy', async (req, res) => {
  if (!req.user) {
    return res.status(401).json({ success: false, message: 'Giriş yapmalısınız!' });
  }

  const { score } = req.body;
  const gameCost = 1000;

  try {
    const userCoins = await GameUtils.getUserCoins(req.user.id);
    if (userCoins < gameCost) {
      return res.json({ success: false, message: 'Yetersiz coin!' });
    }

    const canPlay = await GameUtils.checkGameLimit(req.user.id, 'flappyBird');
    if (!canPlay) {
      return res.json({ success: false, message: 'Günlük oyun hakkınız doldu!' });
    }

    // Coin'i düş
    await GameUtils.updateUserCoins(req.user.id, -gameCost);

    // Skor bazlı ödül
    const coinReward = Math.max(0, score * 50);
    const xpReward = Math.max(10, score * 5);

    if (coinReward > 0) {
      await GameUtils.updateUserCoins(req.user.id, coinReward);
    }
    await GameUtils.updateUserXP(req.user.id, xpReward);
    await GameUtils.updateGameLimit(req.user.id, 'flappyBird');

    // Yüksek skor için eşya ver
    let item = null;
    if (score >= 5 && Math.random() < 0.4) {
      item = await GameUtils.giveRandomItem(req.user.id, 'flappy');
    }

    res.json({
      success: true,
      score,
      coinReward,
      xpReward,
      item: item ? { name: item.name, icon: item.icon, rarity: item.rarity } : null
    });
  } catch (error) {
    console.error('Flappy bird hatası:', error);
    res.status(500).json({ success: false, message: 'Bir hata oluştu!' });
  }
});

// Şifre oyunu oyna
router.post('/play-password', async (req, res) => {
  if (!req.user) {
    return res.status(401).json({ success: false, message: 'Giriş yapmalısınız!' });
  }

  const { won } = req.body;
  const gameCost = 500;

  try {
    const userCoins = await GameUtils.getUserCoins(req.user.id);
    if (userCoins < gameCost) {
      return res.json({ success: false, message: 'Yetersiz coin!' });
    }

    const canPlay = await GameUtils.checkGameLimit(req.user.id, 'passwordGame');
    if (!canPlay) {
      return res.json({ success: false, message: 'Günlük oyun hakkınız doldu!' });
    }

    // Coin'i düş
    await GameUtils.updateUserCoins(req.user.id, -gameCost);

    let coinReward = 0;
    let xpReward = 15;

    if (won) {
      coinReward = gameCost * 3; // 3x kazanç
      xpReward = 75;
      await GameUtils.updateUserCoins(req.user.id, coinReward);
    }

    await GameUtils.updateUserXP(req.user.id, xpReward);
    await GameUtils.updateGameLimit(req.user.id, 'passwordGame');

    // Kazanırsa eşya ver (%25 şans)
    let item = null;
    if (won && Math.random() < 0.25) {
      item = await GameUtils.giveRandomItem(req.user.id, 'password');
    }

    res.json({
      success: true,
      won,
      coinReward,
      xpReward,
      item: item ? { name: item.name, icon: item.icon, rarity: item.rarity } : null
    });
  } catch (error) {
    console.error('Şifre oyunu hatası:', error);
    res.status(500).json({ success: false, message: 'Bir hata oluştu!' });
  }
});

// Kullanıcı istatistikleri
router.get('/user-stats', async (req, res) => {
  if (!req.user) {
    return res.status(401).json({ success: false, message: 'Giriş yapmalısınız!' });
  }

  try {
    const limits = await GameLimits.findOne({ userId: req.user.id });
    const skills = await MiningSkills.findOne({ userId: req.user.id });
    const userCoins = await GameUtils.getUserCoins(req.user.id);

    res.json({
      success: true,
      coins: userCoins,
      limits: limits || {},
      skills: skills || {}
    });
  } catch (error) {
    console.error('İstatistik hatası:', error);
    res.status(500).json({ success: false, message: 'Bir hata oluştu!' });
  }
});

module.exports = router;