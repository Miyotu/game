const { Client, GatewayIntentBits,  Partials, REST, Routes, PermissionsBitField, EmbedBuilder, AuditLogEvent, Collection } = require("discord.js");
const DiscordTicket = require('./src/models/discordticket');
const express = require("express");
const app = express();
const conf = require("./src/configs/config.json");
const settings = require("./src/configs/settings.json");
const bodyParser = require("body-parser");
const jsonconfig = require("./src/configs/config.json");
const { config } = require('./src/configs/config.js');
const { Database } = require("quickmongo");
const cookieParser = require("cookie-parser");
const ejs = require("ejs");
const path = require("path");
const passport = require("passport");
const axios = require('axios');
const { Strategy } = require("passport-discord");
const session = require("express-session");
const mongoose = require("mongoose");
const url = require("url");
const moment = require("moment");
const fs = require("fs");
const router = express.Router();
const multer = require('multer');
require("moment-duration-format");
const { PermissionFlagsBits } = require('discord.js');
const BotDurum = require('./src/models/botdurum');
const Ticket = require("./src/models/ticket")
const Updates = require("./src/models/updates")
const Shop = require("./src/models/shop")
const Daily = require("./src/models/daily");
const Inventory = require("./src/models/inventory");
const { Events } = require('discord.js');
const nodemailer = require('nodemailer');
const ChannelProtection = require('./src/models/ChannelProtection'); // dosya yolu sana göre değişebilir
const RoleProtection = require('./src/models/RoleProtection');
const LinkProtection = require('./src/models/LinkProtection');
const http = require("http");
const socketIo = require("socket.io");
const { spawn } = require("child_process");
const server = http.createServer(app);
const io = socketIo(server);
const { Davet } = require('./src/models/davet');

moment.locale("tr");
const cooldown = new Map();
const client = new Client({
  intents: [
    GatewayIntentBits.Guilds,                    // Sunucu bilgisi
    GatewayIntentBits.GuildMessages,             // Sunucudaki mesajları görebilme
    GatewayIntentBits.MessageContent,            // Mesaj içeriğine erişim (ayar panelinden açılmış olmalı)
    GatewayIntentBits.GuildMembers,              // Üye listesi ve bilgileri (ayar panelinden açılmış olmalı)
    GatewayIntentBits.GuildPresences,            // Kullanıcı çevrim içi durumları
    GatewayIntentBits.GuildVoiceStates,          // Ses kanalı durumu
    GatewayIntentBits.GuildMessageReactions,     // Mesaj tepkileri
    GatewayIntentBits.DirectMessages,            // DM mesajları
    GatewayIntentBits.GuildInvites,              // Davetleri yönetmek
  ],
  partials: [
    Partials.Channel,         // DM'lerde kanal bilgisi için gerekli
    Partials.Message,         // Kısmi mesajlar
    Partials.Reaction,        // Kısmi tepkiler
    Partials.User,            // Kısmi kullanıcı verisi
    Partials.GuildMember,     // Kısmi guild üyeleri
  ]
});

app.engine('.ejs', ejs.renderFile);
app.set('view engine', 'ejs');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false, }));
app.use(cookieParser());
app.set('views', path.join(__dirname, 'src/views'));
const templateDir = path.resolve(`${process.cwd()}${path.sep}src/views`);
app.use(express.static(__dirname + '/src/public'));
app.use(session({ secret: 'secret-session-thing', resave: false, saveUninitialized: false, }));
app.use(passport.initialize());
app.use(passport.session());

// Passport kullanıcı serializasyonu ve deserializasyonu
passport.serializeUser((user, done) => done(null, user));
passport.deserializeUser((obj, done) => done(null, obj));

const scopes = ['identify', 'guilds', 'email'];

// Passport Discord OAuth2 stratejisi
passport.use(new Strategy({
  clientID: settings.clientID,
  clientSecret: settings.secret,
  callbackURL: settings.callbackURL,
  scope: scopes,
},
(accessToken, refreshToken, profile, done) => {
  const email = profile.emails && profile.emails.length > 0 ? profile.emails[0].value : null;
  
  // Eğer e-posta bilgisi mevcut ise, profile objesine ekle
  if (email) {
    profile.email = email;
  }
  
  process.nextTick(() => done(null, profile));
}));

async function setBotPresence() {
  try {
    const botDurum = await BotDurum.findOne(); // Veritabanındaki ilk kaydı al

    if (botDurum && botDurum.durumMesaji) {
      // Botun durumunu ayarla
      client.user.setPresence({
        activity: {
          name: botDurum.durumMesaji, // MongoDB'den alınan durum mesajı
          type: 'PLAYING' // Durum türü
        }
      });
      console.log(`Botun durumu ayarlandı: ${botDurum.durumMesaji}`);
    } else {
      console.log('Veritabanında durum mesajı bulunamadı.');
    }
  } catch (err) {
    console.error('Bot durumu ayarlanırken hata oluştu:', err);
  }
}

app.get('/login', passport.authenticate('discord', { scope: scopes, }));
app.get('/callback', passport.authenticate('discord', { failureRedirect: '/error', }), (req, res) => res.redirect('/'));
app.get('/logout', (req, res) => {
  req.logout();
  return res.redirect('/');
});

app.get("/games", async (req, res) => {
  if (!req.user) {
    return res.redirect("/login-error"); // Giriş yapmamış kullanıcılar için giriş sayfasına yönlendirme
  }
  const userID = req.params.userID;
  const lang = req.cookies.lang || "tr"; // Varsayılan dil
  const guild = client.guilds.cache.get(conf.guildID);

  if (!translations[lang]) {
    console.warn(`Geçersiz dil: ${lang}. Varsayılan olarak 'tr' kullanılıyor.`);
  }
const member = await guild.members.fetch(req.user.id);
    let auth;
if (guild.roles.cache.has(conf.bsahip)) auth = "Bot Sahibi";
else if (guild.roles.cache.has(conf.admin)) auth = "Taxperia Yetkili";

let isStaff;
if (guild.roles.cache.has(conf.ownerRole)) isStaff = "Owner";

res.render("games", {
user: req.user,
member : req.isAuthenticated() ? req.user : null,
guild,
translations: translations[lang],
conf,
auth,
icon: client.guilds.cache.get(conf.guildID).iconURL({ dynamic: true }),
bot: client,
path: req.path, 
isStaff,
reqMember: req.user ? client.guilds.cache.get(conf.guildID).members.cache.get(req.user.id) : null
});

client.login(settings.token).catch((err) => console.log(err));
