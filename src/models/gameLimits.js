const mongoose = require('mongoose');

const gameLimitsSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  dailyReward: {
    lastClaimed: { type: Date },
    canClaim: { type: Boolean, default: true }
  },
  rockPaperScissors: {
    playsToday: { type: Number, default: 0 },
    lastPlayDate: { type: Date },
    maxPlaysPerDay: { type: Number, default: 3 }
  },
  passwordGame: {
    playsToday: { type: Number, default: 0 },
    lastPlayDate: { type: Date },
    maxPlaysPerDay: { type: Number, default: 5 }
  },
  mining: {
    lastMineTime: { type: Date },
    cooldownMinutes: { type: Number, default: 30 }
  },
  flappyBird: {
    playsToday: { type: Number, default: 0 },
    lastPlayDate: { type: Date },
    maxPlaysPerDay: { type: Number, default: 10 }
  }
});

module.exports = mongoose.model('GameLimits', gameLimitsSchema);