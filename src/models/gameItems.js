const mongoose = require('mongoose');

const gameItemsSchema = new mongoose.Schema({
  itemId: { type: String, required: true, unique: true },
  name: { type: String, required: true },
  description: { type: String, required: true },
  rarity: { type: String, required: true }, // common, rare, epic, legendary
  value: { type: Number, required: true },
  icon: { type: String, required: true },
  dropRate: { type: Number, required: true }, // 0-100 percentage
  gameSource: { type: String, required: true } // mining, flappy, rps, password, daily
});

// Varsayılan eşyaları ekle
gameItemsSchema.statics.initializeItems = async function() {
  const items = [
    // Madencilik eşyaları
    { itemId: 'coal', name: 'Kömür', description: 'Sıradan bir kömür parçası', rarity: 'common', value: 50, icon: '⚫', dropRate: 40, gameSource: 'mining' },
    { itemId: 'iron', name: 'Demir', description: 'Değerli demir cevheri', rarity: 'common', value: 100, icon: '🔩', dropRate: 30, gameSource: 'mining' },
    { itemId: 'gold', name: 'Altın', description: 'Parlak altın cevheri', rarity: 'rare', value: 500, icon: '🥇', dropRate: 15, gameSource: 'mining' },
    { itemId: 'diamond', name: 'Elmas', description: 'Nadir elmas taşı', rarity: 'epic', value: 2000, icon: '💎', dropRate: 8, gameSource: 'mining' },
    { itemId: 'emerald', name: 'Zümrüt', description: 'Efsanevi zümrüt', rarity: 'legendary', value: 5000, icon: '💚', dropRate: 2, gameSource: 'mining' },
    
    // Günlük ödül eşyaları
    { itemId: 'gift_box', name: 'Hediye Kutusu', description: 'Günlük hediye kutusu', rarity: 'common', value: 200, icon: '🎁', dropRate: 50, gameSource: 'daily' },
    { itemId: 'lucky_coin', name: 'Şanslı Para', description: 'Şans getiren para', rarity: 'rare', value: 1000, icon: '🪙', dropRate: 20, gameSource: 'daily' },
    { itemId: 'treasure_chest', name: 'Hazine Sandığı', description: 'Değerli hazine sandığı', rarity: 'epic', value: 3000, icon: '📦', dropRate: 5, gameSource: 'daily' },
    
    // Uçan kuş eşyaları
    { itemId: 'feather', name: 'Tüy', description: 'Renkli kuş tüyü', rarity: 'common', value: 75, icon: '🪶', dropRate: 35, gameSource: 'flappy' },
    { itemId: 'golden_feather', name: 'Altın Tüy', description: 'Nadir altın tüy', rarity: 'rare', value: 800, icon: '✨', dropRate: 10, gameSource: 'flappy' },
    
    // Taş kağıt makas eşyaları
    { itemId: 'victory_medal', name: 'Zafer Madalyası', description: 'Kazanma madalyası', rarity: 'rare', value: 600, icon: '🏅', dropRate: 25, gameSource: 'rps' },
    
    // Şifre oyunu eşyaları
    { itemId: 'key', name: 'Anahtar', description: 'Gizli anahtar', rarity: 'rare', value: 400, icon: '🗝️', dropRate: 20, gameSource: 'password' }
  ];

  for (const item of items) {
    await this.findOneAndUpdate(
      { itemId: item.itemId },
      item,
      { upsert: true, new: true }
    );
  }
};

module.exports = mongoose.model('GameItems', gameItemsSchema);