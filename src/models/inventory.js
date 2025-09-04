const mongoose = require('mongoose');

const inventorySchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  items: [{
    itemId: { type: String, required: true },
    name: { type: String, required: true },
    rarity: { type: String, required: true }, // common, rare, epic, legendary
    quantity: { type: Number, default: 1 },
    obtainedAt: { type: Date, default: Date.now }
  }],
  totalValue: { type: Number, default: 0 },
  lastUpdated: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Inventory', inventorySchema);