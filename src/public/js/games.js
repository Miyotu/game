// Oyun modalları ve genel fonksiyonlar
document.addEventListener('DOMContentLoaded', function() {
    // Modal açma/kapama
    const gameCards = document.querySelectorAll('.game-card[data-modal-id]');
    const modals = document.querySelectorAll('.modal');
    const closeButtons = document.querySelectorAll('.close-modal-button');

    gameCards.forEach(card => {
        card.addEventListener('click', function() {
            const modalId = this.getAttribute('data-modal-id');
            const modal = document.getElementById(modalId);
            if (modal) {
                modal.style.display = 'block';
                document.body.style.overflow = 'hidden';
            }
        });
    });

    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const modalId = this.getAttribute('data-modal-id');
            const modal = document.getElementById(modalId);
            if (modal) {
                modal.style.display = 'none';
                document.body.style.overflow = 'auto';
            }
        });
    });

    // Modal dışına tıklayınca kapat
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.style.display = 'none';
                document.body.style.overflow = 'auto';
            }
        });
    });

    // Kullanıcı istatistiklerini yükle
    loadUserStats();
});

// API çağrıları için yardımcı fonksiyon
async function makeGameRequest(endpoint, data = {}) {
    try {
        const response = await fetch(`/api/games/${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        return await response.json();
    } catch (error) {
        console.error('API hatası:', error);
        return { success: false, message: 'Bağlantı hatası!' };
    }
}

// Kullanıcı istatistiklerini yükle
async function loadUserStats() {
    try {
        const response = await fetch('/api/games/user-stats');
        const data = await response.json();
        
        if (data.success) {
            updateCoinDisplays(data.coins);
            updateGameLimits(data.limits);
            updateMiningSkills(data.skills);
        }
    } catch (error) {
        console.error('İstatistik yükleme hatası:', error);
    }
}

// Coin gösterimlerini güncelle
function updateCoinDisplays(coins) {
    const coinElements = document.querySelectorAll('[id*="CoinText"], .player-coins');
    coinElements.forEach(element => {
        element.textContent = `Coin: ${coins.toLocaleString()}`;
    });
}

// Oyun limitlerini güncelle
function updateGameLimits(limits) {
    // RPS kalan hak
    const rpsRemainingElement = document.getElementById('rpsPlaysRemainingText');
    if (rpsRemainingElement && limits.rockPaperScissors) {
        const remaining = limits.rockPaperScissors.maxPlaysPerDay - (limits.rockPaperScissors.playsToday || 0);
        rpsRemainingElement.textContent = `Kalan Hak: ${remaining}`;
    }

    // Password kalan hak
    const passwordRemainingElement = document.getElementById('passwordGamePlaysRemainingText');
    if (passwordRemainingElement && limits.passwordGame) {
        const remaining = limits.passwordGame.maxPlaysPerDay - (limits.passwordGame.playsToday || 0);
        passwordRemainingElement.textContent = `Kalan Hak: ${remaining}`;
    }
}

// Madencilik becerilerini güncelle
function updateMiningSkills(skills) {
    if (!skills) return;

    const skillButtons = {
        'skillFasterMining': skills.fasterMining,
        'skillLuckBoost': skills.luckBoost,
        'skillX2Miner': skills.x2Miner
    };

    Object.entries(skillButtons).forEach(([buttonId, owned]) => {
        const button = document.getElementById(buttonId);
        if (button) {
            if (owned) {
                button.textContent = button.textContent.replace(/\(\d+K? Coin\)/, '(Sahip)');
                button.disabled = true;
                button.style.opacity = '0.6';
            }
        }
    });
}

// Başarı mesajı göster
function showSuccessMessage(message, rewards = {}) {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'fixed top-4 right-4 bg-green-600 text-white p-4 rounded-lg shadow-lg z-50 max-w-sm';
    
    let content = `<div class="font-semibold mb-2">${message}</div>`;
    
    if (rewards.coinReward) {
        content += `<div class="text-sm">💰 +${rewards.coinReward.toLocaleString()} Coin</div>`;
    }
    if (rewards.xpReward) {
        content += `<div class="text-sm">⭐ +${rewards.xpReward} XP</div>`;
    }
    if (rewards.item) {
        content += `<div class="text-sm">${rewards.item.icon} ${rewards.item.name} (${rewards.item.rarity})</div>`;
    }
    
    messageDiv.innerHTML = content;
    document.body.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.remove();
    }, 5000);
}

// Hata mesajı göster
function showErrorMessage(message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'fixed top-4 right-4 bg-red-600 text-white p-4 rounded-lg shadow-lg z-50 max-w-sm';
    messageDiv.innerHTML = `<div class="font-semibold">${message}</div>`;
    document.body.appendChild(messageDiv);
    
    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

// Günlük ödül sistemi
if (document.getElementById('dailyRewardCanvas')) {
    const canvas = document.getElementById('dailyRewardCanvas');
    const ctx = canvas.getContext('2d');
    canvas.width = 600;
    canvas.height = 400;

    let chestOpened = false;
    let animationFrame;

    function drawDailyReward() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Arka plan
        const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
        gradient.addColorStop(0, '#1a1a2e');
        gradient.addColorStop(1, '#16213e');
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Sandık çiz
        const chestX = canvas.width / 2 - 50;
        const chestY = canvas.height / 2 - 40;
        
        if (!chestOpened) {
            // Kapalı sandık
            ctx.fillStyle = '#8B4513';
            ctx.fillRect(chestX, chestY, 100, 80);
            
            // Sandık kapağı
            ctx.fillStyle = '#A0522D';
            ctx.fillRect(chestX - 5, chestY - 10, 110, 20);
            
            // Kilit
            ctx.fillStyle = '#FFD700';
            ctx.fillRect(chestX + 45, chestY + 10, 10, 15);
            
            // Tıklama talimatı
            ctx.fillStyle = '#FFFFFF';
            ctx.font = '18px Arial';
            ctx.textAlign = 'center';
            ctx.fillText('Sandığa tıkla!', canvas.width / 2, canvas.height - 50);
        } else {
            // Açık sandık
            ctx.fillStyle = '#8B4513';
            ctx.fillRect(chestX, chestY + 20, 100, 60);
            
            // Açık kapak
            ctx.fillStyle = '#A0522D';
            ctx.fillRect(chestX - 5, chestY - 30, 110, 20);
            
            // Işık efekti
            const lightGradient = ctx.createRadialGradient(canvas.width / 2, chestY, 0, canvas.width / 2, chestY, 100);
            lightGradient.addColorStop(0, 'rgba(255, 215, 0, 0.8)');
            lightGradient.addColorStop(1, 'rgba(255, 215, 0, 0)');
            ctx.fillStyle = lightGradient;
            ctx.fillRect(chestX - 50, chestY - 50, 200, 100);
        }
    }

    canvas.addEventListener('click', async function(e) {
        if (chestOpened) return;

        const result = await makeGameRequest('daily-reward');
        
        if (result.success) {
            chestOpened = true;
            drawDailyReward();
            
            showSuccessMessage('Günlük ödül alındı!', {
                coinReward: result.coinReward,
                xpReward: result.xpReward,
                item: result.item
            });
            
            loadUserStats();
        } else {
            showErrorMessage(result.message);
        }
    });

    drawDailyReward();
}

// Taş Kağıt Makas oyunu
if (document.getElementById('rpsCanvas')) {
    const canvas = document.getElementById('rpsCanvas');
    const ctx = canvas.getContext('2d');
    canvas.width = 600;
    canvas.height = 400;

    let gameState = 'selection'; // selection, playing, result
    let playerChoice = null;
    let botChoice = null;
    let result = null;

    const choices = {
        rock: { emoji: '🪨', name: 'Taş' },
        paper: { emoji: '📄', name: 'Kağıt' },
        scissors: { emoji: '✂️', name: 'Makas' }
    };

    function drawRPS() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Arka plan
        const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
        gradient.addColorStop(0, '#2d1b69');
        gradient.addColorStop(1, '#11998e');
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        if (gameState === 'selection') {
            // Seçim ekranı
            ctx.fillStyle = '#FFFFFF';
            ctx.font = 'bold 24px Arial';
            ctx.textAlign = 'center';
            ctx.fillText('Seçimini Yap!', canvas.width / 2, 80);

            // Seçenekleri çiz
            const choiceKeys = Object.keys(choices);
            choiceKeys.forEach((choice, index) => {
                const x = (canvas.width / 4) * (index + 1);
                const y = canvas.height / 2;
                
                // Seçenek kutusu
                ctx.fillStyle = '#4A5568';
                ctx.fillRect(x - 60, y - 60, 120, 120);
                
                // Emoji
                ctx.font = '48px Arial';
                ctx.fillText(choices[choice].emoji, x, y - 10);
                
                // İsim
                ctx.font = '16px Arial';
                ctx.fillText(choices[choice].name, x, y + 40);
            });
        } else if (gameState === 'result') {
            // Sonuç ekranı
            ctx.fillStyle = '#FFFFFF';
            ctx.font = 'bold 32px Arial';
            ctx.textAlign = 'center';
            
            let resultText = '';
            let resultColor = '#FFFFFF';
            
            if (result === 'win') {
                resultText = 'KAZANDIN!';
                resultColor = '#48BB78';
            } else if (result === 'lose') {
                resultText = 'KAYBETTİN!';
                resultColor = '#F56565';
            } else {
                resultText = 'BERABERE!';
                resultColor = '#ED8936';
            }
            
            ctx.fillStyle = resultColor;
            ctx.fillText(resultText, canvas.width / 2, 80);

            // Seçimleri göster
            ctx.fillStyle = '#FFFFFF';
            ctx.font = '20px Arial';
            ctx.fillText('Sen', canvas.width / 4, 150);
            ctx.fillText('Bot', (canvas.width / 4) * 3, 150);

            // Seçim emojileri
            ctx.font = '64px Arial';
            ctx.fillText(choices[playerChoice].emoji, canvas.width / 4, 220);
            ctx.fillText(choices[botChoice].emoji, (canvas.width / 4) * 3, 220);

            // Yeniden oyna butonu
            ctx.fillStyle = '#4299E1';
            ctx.fillRect(canvas.width / 2 - 80, 300, 160, 50);
            ctx.fillStyle = '#FFFFFF';
            ctx.font = '18px Arial';
            ctx.fillText('Yeniden Oyna', canvas.width / 2, 330);
        }
    }

    canvas.addEventListener('click', async function(e) {
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        if (gameState === 'selection') {
            // Seçim kontrolü
            const choiceKeys = Object.keys(choices);
            choiceKeys.forEach((choice, index) => {
                const choiceX = (canvas.width / 4) * (index + 1);
                const choiceY = canvas.height / 2;
                
                if (x >= choiceX - 60 && x <= choiceX + 60 && y >= choiceY - 60 && y <= choiceY + 60) {
                    playRPS(choice);
                }
            });
        } else if (gameState === 'result') {
            // Yeniden oyna butonu kontrolü
            if (x >= canvas.width / 2 - 80 && x <= canvas.width / 2 + 80 && y >= 300 && y <= 350) {
                gameState = 'selection';
                drawRPS();
            }
        }
    });

    async function playRPS(choice) {
        playerChoice = choice;
        gameState = 'playing';
        
        const gameResult = await makeGameRequest('play-rps', { playerChoice: choice });
        
        if (gameResult.success) {
            botChoice = gameResult.botChoice;
            result = gameResult.result;
            gameState = 'result';
            
            showSuccessMessage('Oyun tamamlandı!', {
                coinReward: gameResult.coinReward,
                xpReward: gameResult.xpReward,
                item: gameResult.item
            });
            
            loadUserStats();
        } else {
            showErrorMessage(gameResult.message);
            gameState = 'selection';
        }
        
        drawRPS();
    }

    drawRPS();
}

// Madencilik oyunu
if (document.getElementById('miningCanvas')) {
    const canvas = document.getElementById('miningCanvas');
    const ctx = canvas.getContext('2d');
    canvas.width = 600;
    canvas.height = 400;

    let miningAnimation = false;
    let pickaxeAngle = 0;

    // Madencilik becerileri
    const skillButtons = document.querySelectorAll('.mining-skill-button');
    skillButtons.forEach(button => {
        button.addEventListener('click', async function() {
            const cost = parseInt(this.getAttribute('data-cost'));
            const skillType = this.id.replace('skill', '').toLowerCase();
            
            // Skill type'ı düzelt
            let apiSkillType = skillType;
            if (skillType === 'fastermining') apiSkillType = 'fasterMining';
            if (skillType === 'luckboost') apiSkillType = 'luckBoost';
            if (skillType === 'x2miner') apiSkillType = 'x2Miner';
            
            const result = await makeGameRequest('buy-mining-skill', { skillType: apiSkillType });
            
            if (result.success) {
                showSuccessMessage(result.message);
                this.textContent = this.textContent.replace(/\(\d+K? Coin\)/, '(Sahip)');
                this.disabled = true;
                this.style.opacity = '0.6';
                loadUserStats();
            } else {
                showErrorMessage(result.message);
            }
        });
    });

    function drawMining() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Arka plan (mağara)
        const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
        gradient.addColorStop(0, '#2c1810');
        gradient.addColorStop(1, '#1a0f08');
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Mağara duvarları
        ctx.fillStyle = '#3d2817';
        ctx.fillRect(0, 0, canvas.width, 50);
        ctx.fillRect(0, canvas.height - 50, canvas.width, 50);

        // Madenci (basit çubuk adam)
        const minerX = canvas.width / 2;
        const minerY = canvas.height / 2 + 50;
        
        // Vücut
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(minerX, minerY - 30);
        ctx.lineTo(minerX, minerY + 30);
        ctx.stroke();

        // Kafa
        ctx.beginPath();
        ctx.arc(minerX, minerY - 40, 10, 0, Math.PI * 2);
        ctx.stroke();

        // Kazma
        const pickaxeX = minerX + Math.cos(pickaxeAngle) * 40;
        const pickaxeY = minerY - 10 + Math.sin(pickaxeAngle) * 20;
        
        ctx.strokeStyle = '#8B4513';
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(minerX + 10, minerY - 20);
        ctx.lineTo(pickaxeX, pickaxeY);
        ctx.stroke();

        // Kazma başı
        ctx.fillStyle = '#696969';
        ctx.fillRect(pickaxeX - 5, pickaxeY - 5, 10, 10);

        // Tıklama talimatı
        if (!miningAnimation) {
            ctx.fillStyle = '#FFFFFF';
            ctx.font = '18px Arial';
            ctx.textAlign = 'center';
            ctx.fillText('Madencilik yapmak için tıkla!', canvas.width / 2, canvas.height - 20);
        }

        // Animasyon
        if (miningAnimation) {
            pickaxeAngle += 0.3;
            if (pickaxeAngle > Math.PI * 2) {
                pickaxeAngle = 0;
                miningAnimation = false;
            }
            requestAnimationFrame(drawMining);
        }
    }

    canvas.addEventListener('click', async function() {
        if (miningAnimation) return;

        miningAnimation = true;
        drawMining();

        const result = await makeGameRequest('play-mining');
        
        if (result.success) {
            showSuccessMessage('Madencilik tamamlandı!', {
                coinReward: result.coinReward,
                xpReward: result.xpReward,
                item: result.item
            });
            loadUserStats();
        } else {
            showErrorMessage(result.message);
            miningAnimation = false;
        }
    });

    drawMining();
}

// Şifre tahmin oyunu
if (document.getElementById('passwordGuessGameContent')) {
    let currentPassword = '';
    let attempts = 0;
    const maxAttempts = 5;

    function initPasswordGame() {
        // 4 haneli rastgele şifre oluştur
        currentPassword = Math.floor(Math.random() * 9000 + 1000).toString();
        attempts = 0;
        
        document.getElementById('passwordInfoText').textContent = 'Şifre 4 haneli.';
        document.getElementById('passwordAttemptsLeft').textContent = `Kalan deneme: ${maxAttempts}`;
        
        // Input alanı oluştur
        const inputContainer = document.getElementById('passwordInputContainer');
        inputContainer.innerHTML = '<input type="number" id="passwordInput" class="bg-gray-700 text-white p-2 rounded" placeholder="4 haneli şifre" min="1000" max="9999">';
        
        document.getElementById('hintDisplay').innerHTML = '';
        document.getElementById('passwordGuessResult').innerHTML = '';
    }

    document.getElementById('submitGuessButton').addEventListener('click', async function() {
        const input = document.getElementById('passwordInput');
        const guess = input.value;
        
        if (guess.length !== 4) {
            showErrorMessage('4 haneli bir sayı girin!');
            return;
        }

        attempts++;
        
        // İpucu ver
        let hint = '';
        let correctDigits = 0;
        
        for (let i = 0; i < 4; i++) {
            if (guess[i] === currentPassword[i]) {
                hint += '🟢';
                correctDigits++;
            } else if (currentPassword.includes(guess[i])) {
                hint += '🟡';
            } else {
                hint += '🔴';
            }
        }
        
        document.getElementById('hintDisplay').innerHTML += `<div>${guess}: ${hint}</div>`;
        document.getElementById('passwordAttemptsLeft').textContent = `Kalan deneme: ${maxAttempts - attempts}`;
        
        if (correctDigits === 4) {
            // Kazandı
            const result = await makeGameRequest('play-password', { won: true });
            if (result.success) {
                document.getElementById('passwordGuessResult').innerHTML = '<div class="text-green-400">🎉 Tebrikler! Şifreyi buldunuz!</div>';
                showSuccessMessage('Şifre bulundu!', {
                    coinReward: result.coinReward,
                    xpReward: result.xpReward,
                    item: result.item
                });
                loadUserStats();
            }
            this.disabled = true;
        } else if (attempts >= maxAttempts) {
            // Kaybetti
            const result = await makeGameRequest('play-password', { won: false });
            document.getElementById('passwordGuessResult').innerHTML = `<div class="text-red-400">😞 Şifre: ${currentPassword}</div>`;
            this.disabled = true;
        }
        
        input.value = '';
    });

    initPasswordGame();
}

// Uçan kuş oyunu (geliştirilmiş)
if (document.getElementById('flappyBirdCanvas')) {
    const canvas = document.getElementById('flappyBirdCanvas');
    const ctx = canvas.getContext('2d');
    canvas.width = 600;
    canvas.height = 400;

    let gameRunning = false;
    let bird = { x: 100, y: 200, velocity: 0, size: 20 };
    let pipes = [];
    let score = 0;
    let gameStarted = false;

    const startButton = document.getElementById('flappyBirdStartButton');
    
    startButton.addEventListener('click', async function() {
        if (gameRunning) return;
        
        // Oyunu başlat
        gameRunning = true;
        gameStarted = true;
        bird = { x: 100, y: 200, velocity: 0, size: 20 };
        pipes = [];
        score = 0;
        
        gameLoop();
    });

    function drawFlappyBird() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Gökyüzü gradyanı
        const skyGradient = ctx.createLinearGradient(0, 0, 0, canvas.height);
        skyGradient.addColorStop(0, '#87CEEB');
        skyGradient.addColorStop(1, '#98FB98');
        ctx.fillStyle = skyGradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Bulutlar
        ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
        ctx.beginPath();
        ctx.arc(150, 80, 30, 0, Math.PI * 2);
        ctx.arc(170, 80, 40, 0, Math.PI * 2);
        ctx.arc(190, 80, 30, 0, Math.PI * 2);
        ctx.fill();

        ctx.beginPath();
        ctx.arc(400, 120, 25, 0, Math.PI * 2);
        ctx.arc(415, 120, 35, 0, Math.PI * 2);
        ctx.arc(430, 120, 25, 0, Math.PI * 2);
        ctx.fill();

        if (!gameStarted) {
            // Başlangıç ekranı
            ctx.fillStyle = '#FFFFFF';
            ctx.font = 'bold 24px Arial';
            ctx.textAlign = 'center';
            ctx.fillText('Uçan Kuş Macerası', canvas.width / 2, 150);
            ctx.font = '16px Arial';
            ctx.fillText('Başla butonuna tıkla ve boşluk tuşu ile uç!', canvas.width / 2, 180);
            
            // Demo kuş
            ctx.fillStyle = '#FFD700';
            ctx.beginPath();
            ctx.arc(canvas.width / 2, 220, 15, 0, Math.PI * 2);
            ctx.fill();
            
            // Kuş gözü
            ctx.fillStyle = '#000000';
            ctx.beginPath();
            ctx.arc(canvas.width / 2 + 5, 215, 3, 0, Math.PI * 2);
            ctx.fill();
            
            return;
        }

        // Borular
        pipes.forEach(pipe => {
            ctx.fillStyle = '#228B22';
            ctx.fillRect(pipe.x, 0, pipe.width, pipe.topHeight);
            ctx.fillRect(pipe.x, pipe.topHeight + pipe.gap, pipe.width, canvas.height - pipe.topHeight - pipe.gap);
            
            // Boru kenarları
            ctx.fillStyle = '#32CD32';
            ctx.fillRect(pipe.x - 5, pipe.topHeight - 20, pipe.width + 10, 20);
            ctx.fillRect(pipe.x - 5, pipe.topHeight + pipe.gap, pipe.width + 10, 20);
        });

        // Kuş
        ctx.fillStyle = '#FFD700';
        ctx.beginPath();
        ctx.arc(bird.x, bird.y, bird.size, 0, Math.PI * 2);
        ctx.fill();
        
        // Kuş gözü
        ctx.fillStyle = '#000000';
        ctx.beginPath();
        ctx.arc(bird.x + 8, bird.y - 5, 4, 0, Math.PI * 2);
        ctx.fill();
        
        // Kuş gagası
        ctx.fillStyle = '#FF8C00';
        ctx.beginPath();
        ctx.moveTo(bird.x + bird.size, bird.y);
        ctx.lineTo(bird.x + bird.size + 10, bird.y - 3);
        ctx.lineTo(bird.x + bird.size + 10, bird.y + 3);
        ctx.closePath();
        ctx.fill();

        // Skor
        ctx.fillStyle = '#FFFFFF';
        ctx.font = 'bold 24px Arial';
        ctx.textAlign = 'left';
        ctx.fillText(`Skor: ${score}`, 20, 40);
    }

    function gameLoop() {
        if (!gameRunning) return;

        // Kuş fiziği
        bird.velocity += 0.5; // Yerçekimi
        bird.y += bird.velocity;

        // Boru oluştur
        if (pipes.length === 0 || pipes[pipes.length - 1].x < canvas.width - 200) {
            const gapY = Math.random() * (canvas.height - 200) + 100;
            pipes.push({
                x: canvas.width,
                width: 50,
                topHeight: gapY - 75,
                gap: 150
            });
        }

        // Boruları hareket ettir
        pipes.forEach((pipe, index) => {
            pipe.x -= 3;
            
            // Skor artır
            if (pipe.x + pipe.width < bird.x && !pipe.scored) {
                score++;
                pipe.scored = true;
                document.getElementById('flappyBirdScoreText').textContent = `Skor: ${score}`;
            }
            
            // Çarpışma kontrolü
            if (bird.x + bird.size > pipe.x && bird.x - bird.size < pipe.x + pipe.width) {
                if (bird.y - bird.size < pipe.topHeight || bird.y + bird.size > pipe.topHeight + pipe.gap) {
                    endGame();
                    return;
                }
            }
        });

        // Ekran dışındaki boruları temizle
        pipes = pipes.filter(pipe => pipe.x + pipe.width > 0);

        // Zemin/tavan çarpışması
        if (bird.y + bird.size > canvas.height || bird.y - bird.size < 0) {
            endGame();
            return;
        }

        drawFlappyBird();
        requestAnimationFrame(gameLoop);
    }

    async function endGame() {
        gameRunning = false;
        
        const result = await makeGameRequest('play-flappy', { score });
        
        if (result.success) {
            showSuccessMessage(`Oyun bitti! Skor: ${score}`, {
                coinReward: result.coinReward,
                xpReward: result.xpReward,
                item: result.item
            });
            loadUserStats();
        }
        
        // Oyunu sıfırla
        setTimeout(() => {
            gameStarted = false;
            bird = { x: 100, y: 200, velocity: 0, size: 20 };
            pipes = [];
            score = 0;
            document.getElementById('flappyBirdScoreText').textContent = 'Skor: 0';
            drawFlappyBird();
        }, 2000);
    }

    // Spacebar ile zıplama
    document.addEventListener('keydown', function(e) {
        if (e.code === 'Space' && gameRunning) {
            e.preventDefault();
            bird.velocity = -8;
        }
    });

    // Canvas'a tıklayarak da zıplama
    canvas.addEventListener('click', function() {
        if (gameRunning) {
            bird.velocity = -8;
        }
    });

    drawFlappyBird();
}