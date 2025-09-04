const express = require('express');
const router = express.Router();
const Inventory = require('../models/inventory');
const GameItems = require('../models/gameItems');
const Daily = require('../models/daily');

// Envanter sayfası
router.get('/', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login');
  }

  try {
    const userInventory = await Inventory.findOne({ userId: req.user.id });
    const userDaily = await Daily.findOne({ userId: req.user.id });
    const allItems = await GameItems.find();
    
    const guild = req.app.locals.client.guilds.cache.get(req.app.locals.conf.guildID);
    const member = await guild.members.fetch(req.user.id);
    
    let auth;
    if (member.roles.cache.has(req.app.locals.conf.bsahip)) auth = "Bot Sahibi";
    else if (member.roles.cache.has(req.app.locals.conf.admin)) auth = "Taxperia Yetkili";

    let isStaff;
    if (member.roles.cache.has(req.app.locals.conf.ownerRole)) isStaff = "Owner";

    res.render('inventory', {
      user: req.user,
      member: req.user,
      guild,
      conf: req.app.locals.conf,
      auth,
      isStaff,
      icon: guild.iconURL({ dynamic: true }),
      bot: req.app.locals.client,
      path: req.path,
      reqMember: member,
      inventory: userInventory,
      userCoins: userDaily ? userDaily.coin : 0,
      userXP: userDaily ? userDaily.xp : 0,
      allItems
    });
  } catch (error) {
    console.error('Envanter yüklenirken hata:', error);
    res.status(500).send('Bir hata oluştu');
  }
});

module.exports = router;