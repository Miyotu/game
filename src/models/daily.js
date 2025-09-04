// daily.js

const mongoose = require('mongoose');

const dailySchema = new mongoose.Schema({
  nickname: { type: String },
  userId: { type: String, required: true, unique: true },
  coin: { type: Number, required: true, default: 0 },
  xp: { type: Number, required: true, default: 0 },
  timestamp: { type: Date, required: true, default: Date.now }
});

module.exports = mongoose.model('Daily', dailySchema);
