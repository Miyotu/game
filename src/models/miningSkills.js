const mongoose = require('mongoose');

const miningSkillsSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  fasterMining: { type: Boolean, default: false },
  luckBoost: { type: Boolean, default: false },
  x2Miner: { type: Boolean, default: false },
  purchasedAt: {
    fasterMining: { type: Date },
    luckBoost: { type: Date },
    x2Miner: { type: Date }
  },
  totalSpent: { type: Number, default: 0 }
});

module.exports = mongoose.model('MiningSkills', miningSkillsSchema);