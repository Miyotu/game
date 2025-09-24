-- MiyotuGame Plugin Database Schema
-- MySQL Database Schema for Minecraft 1.20.1 Plugin
-- 
-- This file contains all table structures needed for the MiyotuGame plugin
-- Run this file after creating your database to set up the tables manually
-- Note: The plugin will create these tables automatically on first run

-- Use the miyotugame database
USE miyotugame;

-- Players table - stores all player data
CREATE TABLE IF NOT EXISTS mg_players (
    uuid VARCHAR(36) PRIMARY KEY COMMENT 'Player UUID',
    username VARCHAR(16) NOT NULL COMMENT 'Player username',
    balance DECIMAL(15,2) DEFAULT 1000.00 COMMENT 'Player currency balance',
    rank VARCHAR(32) DEFAULT 'player' COMMENT 'Player rank',
    god_mode BOOLEAN DEFAULT FALSE COMMENT 'God mode status',
    fly_mode BOOLEAN DEFAULT FALSE COMMENT 'Flight mode status',
    last_daily BIGINT DEFAULT 0 COMMENT 'Last daily reward claim timestamp',
    daily_streak INT DEFAULT 0 COMMENT 'Daily reward streak count',
    playtime BIGINT DEFAULT 0 COMMENT 'Total playtime in milliseconds',
    first_join BIGINT DEFAULT 0 COMMENT 'First join timestamp',
    last_seen BIGINT DEFAULT 0 COMMENT 'Last seen timestamp',
    language VARCHAR(5) DEFAULT 'tr' COMMENT 'Player language preference',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record update time',
    
    INDEX idx_username (username),
    INDEX idx_rank (rank),
    INDEX idx_balance (balance),
    INDEX idx_last_seen (last_seen)
) ENGINE=InnoDB COMMENT='Player data and statistics';

-- Friends table - stores player friendships
CREATE TABLE IF NOT EXISTS mg_friends (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Friendship ID',
    player_uuid VARCHAR(36) NOT NULL COMMENT 'Player UUID',
    friend_uuid VARCHAR(36) NOT NULL COMMENT 'Friend UUID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Friendship creation time',
    
    FOREIGN KEY (player_uuid) REFERENCES mg_players(uuid) ON DELETE CASCADE,
    FOREIGN KEY (friend_uuid) REFERENCES mg_players(uuid) ON DELETE CASCADE,
    UNIQUE KEY unique_friendship (player_uuid, friend_uuid),
    INDEX idx_player (player_uuid),
    INDEX idx_friend (friend_uuid)
) ENGINE=InnoDB COMMENT='Player friendships';

-- Regions table - stores protected regions
CREATE TABLE IF NOT EXISTS mg_regions (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Region ID',
    name VARCHAR(64) NOT NULL UNIQUE COMMENT 'Region name',
    world VARCHAR(64) NOT NULL COMMENT 'World name',
    min_x INT NOT NULL COMMENT 'Minimum X coordinate',
    min_y INT NOT NULL COMMENT 'Minimum Y coordinate',
    min_z INT NOT NULL COMMENT 'Minimum Z coordinate',
    max_x INT NOT NULL COMMENT 'Maximum X coordinate',
    max_y INT NOT NULL COMMENT 'Maximum Y coordinate',
    max_z INT NOT NULL COMMENT 'Maximum Z coordinate',
    owner_uuid VARCHAR(36) COMMENT 'Region owner UUID',
    pvp BOOLEAN DEFAULT FALSE COMMENT 'PvP enabled in region',
    build BOOLEAN DEFAULT FALSE COMMENT 'Building allowed in region',
    interact BOOLEAN DEFAULT FALSE COMMENT 'Interaction allowed in region',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Region creation time',
    
    FOREIGN KEY (owner_uuid) REFERENCES mg_players(uuid) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_world (world),
    INDEX idx_owner (owner_uuid),
    INDEX idx_coordinates (world, min_x, min_z, max_x, max_z)
) ENGINE=InnoDB COMMENT='Protected regions';

-- Parkour courses table
CREATE TABLE IF NOT EXISTS mg_parkour (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Parkour course ID',
    name VARCHAR(64) NOT NULL UNIQUE COMMENT 'Parkour course name',
    world VARCHAR(64) NOT NULL COMMENT 'World name',
    start_x DOUBLE NOT NULL COMMENT 'Start X coordinate',
    start_y DOUBLE NOT NULL COMMENT 'Start Y coordinate',
    start_z DOUBLE NOT NULL COMMENT 'Start Z coordinate',
    end_x DOUBLE NOT NULL COMMENT 'End X coordinate',
    end_y DOUBLE NOT NULL COMMENT 'End Y coordinate',
    end_z DOUBLE NOT NULL COMMENT 'End Z coordinate',
    reward DECIMAL(15,2) DEFAULT 1000.00 COMMENT 'Completion reward',
    difficulty VARCHAR(16) DEFAULT 'MEDIUM' COMMENT 'Difficulty level',
    created_by VARCHAR(36) COMMENT 'Creator UUID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Course creation time',
    
    FOREIGN KEY (created_by) REFERENCES mg_players(uuid) ON DELETE SET NULL,
    INDEX idx_name (name),
    INDEX idx_world (world),
    INDEX idx_difficulty (difficulty),
    INDEX idx_creator (created_by)
) ENGINE=InnoDB COMMENT='Parkour courses';

-- Parkour records table
CREATE TABLE IF NOT EXISTS mg_parkour_records (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Record ID',
    parkour_id INT NOT NULL COMMENT 'Parkour course ID',
    player_uuid VARCHAR(36) NOT NULL COMMENT 'Player UUID',
    completion_time BIGINT NOT NULL COMMENT 'Completion time in milliseconds',
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Completion timestamp',
    
    FOREIGN KEY (parkour_id) REFERENCES mg_parkour(id) ON DELETE CASCADE,
    FOREIGN KEY (player_uuid) REFERENCES mg_players(uuid) ON DELETE CASCADE,
    UNIQUE KEY unique_record (parkour_id, player_uuid),
    INDEX idx_parkour (parkour_id),
    INDEX idx_player (player_uuid),
    INDEX idx_time (completion_time),
    INDEX idx_completed_at (completed_at)
) ENGINE=InnoDB COMMENT='Parkour completion records';

-- Bans table - stores player bans
CREATE TABLE IF NOT EXISTS mg_bans (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Ban ID',
    player_uuid VARCHAR(36) NOT NULL COMMENT 'Banned player UUID',
    player_name VARCHAR(16) NOT NULL COMMENT 'Banned player name',
    banned_by VARCHAR(36) COMMENT 'Admin who issued ban',
    reason TEXT COMMENT 'Ban reason',
    banned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Ban timestamp',
    expires_at TIMESTAMP NULL COMMENT 'Ban expiration (NULL for permanent)',
    active BOOLEAN DEFAULT TRUE COMMENT 'Ban active status',
    
    FOREIGN KEY (banned_by) REFERENCES mg_players(uuid) ON DELETE SET NULL,
    INDEX idx_player (player_uuid),
    INDEX idx_player_name (player_name),
    INDEX idx_active (active),
    INDEX idx_expires (expires_at),
    INDEX idx_banned_at (banned_at)
) ENGINE=InnoDB COMMENT='Player bans';

-- Logs table - stores all system logs
CREATE TABLE IF NOT EXISTS mg_logs (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Log ID',
    event_type VARCHAR(32) NOT NULL COMMENT 'Type of event',
    player_uuid VARCHAR(36) COMMENT 'Player UUID involved',
    player_name VARCHAR(16) COMMENT 'Player name involved',
    action VARCHAR(255) NOT NULL COMMENT 'Action performed',
    details TEXT COMMENT 'Additional details',
    world VARCHAR(64) COMMENT 'World where action occurred',
    x DOUBLE COMMENT 'X coordinate',
    y DOUBLE COMMENT 'Y coordinate',
    z DOUBLE COMMENT 'Z coordinate',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Log timestamp',
    
    INDEX idx_event_type (event_type),
    INDEX idx_player (player_uuid),
    INDEX idx_player_name (player_name),
    INDEX idx_created_at (created_at),
    INDEX idx_world (world),
    INDEX idx_action (action)
) ENGINE=InnoDB COMMENT='System activity logs';

-- Economy transactions table
CREATE TABLE IF NOT EXISTS mg_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Transaction ID',
    from_uuid VARCHAR(36) COMMENT 'Sender UUID (NULL for system)',
    to_uuid VARCHAR(36) COMMENT 'Receiver UUID (NULL for system)',
    amount DECIMAL(15,2) NOT NULL COMMENT 'Transaction amount',
    transaction_type VARCHAR(32) NOT NULL COMMENT 'Type of transaction',
    description TEXT COMMENT 'Transaction description',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Transaction timestamp',
    
    INDEX idx_from (from_uuid),
    INDEX idx_to (to_uuid),
    INDEX idx_type (transaction_type),
    INDEX idx_amount (amount),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='Economy transactions';

-- Quiz questions table (for quiz mini-game)
CREATE TABLE IF NOT EXISTS mg_quiz_questions (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Question ID',
    question TEXT NOT NULL COMMENT 'Question text',
    correct_answer VARCHAR(255) NOT NULL COMMENT 'Correct answer',
    wrong_answers JSON COMMENT 'Array of wrong answers',
    category VARCHAR(32) DEFAULT 'general' COMMENT 'Question category',
    difficulty VARCHAR(16) DEFAULT 'medium' COMMENT 'Question difficulty',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Question creation time',
    
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty)
) ENGINE=InnoDB COMMENT='Quiz questions';

-- Quiz scores table
CREATE TABLE IF NOT EXISTS mg_quiz_scores (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Score ID',
    player_uuid VARCHAR(36) NOT NULL COMMENT 'Player UUID',
    score INT NOT NULL COMMENT 'Quiz score',
    questions_answered INT NOT NULL COMMENT 'Number of questions answered',
    correct_answers INT NOT NULL COMMENT 'Number of correct answers',
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Quiz completion time',
    
    FOREIGN KEY (player_uuid) REFERENCES mg_players(uuid) ON DELETE CASCADE,
    INDEX idx_player (player_uuid),
    INDEX idx_score (score),
    INDEX idx_completed_at (completed_at)
) ENGINE=InnoDB COMMENT='Quiz scores and statistics';

-- Treasure hunts table
CREATE TABLE IF NOT EXISTS mg_treasure_hunts (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Hunt ID',
    name VARCHAR(64) NOT NULL COMMENT 'Hunt name',
    world VARCHAR(64) NOT NULL COMMENT 'World name',
    treasure_x DOUBLE NOT NULL COMMENT 'Treasure X coordinate',
    treasure_y DOUBLE NOT NULL COMMENT 'Treasure Y coordinate',
    treasure_z DOUBLE NOT NULL COMMENT 'Treasure Z coordinate',
    hints JSON COMMENT 'Array of hints',
    reward DECIMAL(15,2) DEFAULT 500.00 COMMENT 'Hunt reward',
    active BOOLEAN DEFAULT TRUE COMMENT 'Hunt active status',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Hunt creation time',
    
    INDEX idx_name (name),
    INDEX idx_world (world),
    INDEX idx_active (active)
) ENGINE=InnoDB COMMENT='Treasure hunts';

-- Player treasure hunt progress
CREATE TABLE IF NOT EXISTS mg_treasure_progress (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Progress ID',
    hunt_id INT NOT NULL COMMENT 'Hunt ID',
    player_uuid VARCHAR(36) NOT NULL COMMENT 'Player UUID',
    current_hint INT DEFAULT 0 COMMENT 'Current hint index',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Hunt start time',
    completed_at TIMESTAMP NULL COMMENT 'Hunt completion time',
    
    FOREIGN KEY (hunt_id) REFERENCES mg_treasure_hunts(id) ON DELETE CASCADE,
    FOREIGN KEY (player_uuid) REFERENCES mg_players(uuid) ON DELETE CASCADE,
    UNIQUE KEY unique_hunt_progress (hunt_id, player_uuid),
    INDEX idx_hunt (hunt_id),
    INDEX idx_player (player_uuid),
    INDEX idx_started_at (started_at)
) ENGINE=InnoDB COMMENT='Player treasure hunt progress';

-- Insert some sample data

-- Sample quiz questions
INSERT INTO mg_quiz_questions (question, correct_answer, wrong_answers, category, difficulty) VALUES
('Minecraft\'da hangi blok en sert bloktur?', 'Bedrock', '["Obsidian", "Diamond Block", "Netherite Block"]', 'minecraft', 'easy'),
('Creeper\'lar hangi ses çıkarır?', 'Hissing', '["Growling", "Roaring", "Clicking"]', 'minecraft', 'easy'),
('Nether\'a gitmek için hangi portal kullanılır?', 'Nether Portal', '["End Portal", "Aether Portal", "Twilight Portal"]', 'minecraft', 'easy'),
('Türkiye\'nin başkenti neresidir?', 'Ankara', '["İstanbul", "İzmir", "Bursa"]', 'geography', 'easy'),
('Dünyanın en büyük okyanusu hangisidir?', 'Pasifik Okyanusu', '["Atlantik Okyanusu", "Hint Okyanusu", "Arktik Okyanusu"]', 'geography', 'medium');

-- Sample treasure hunts
INSERT INTO mg_treasure_hunts (name, world, treasure_x, treasure_y, treasure_z, hints, reward, active) VALUES
('Gizli Hazine', 'world', 100.5, 64.0, 200.5, '["Spawn yakınında bir yerde...", "Büyük bir ağacın altında", "X işareti yerini gösterir"]', 1000.00, true),
('Kayıp Define', 'world', -50.0, 70.0, -100.0, '["Batıya doğru yürü...", "Bir mağara ara", "Parıltıyı takip et"]', 1500.00, true);

-- Create indexes for better performance
CREATE INDEX idx_logs_composite ON mg_logs(event_type, created_at);
CREATE INDEX idx_transactions_composite ON mg_transactions(transaction_type, created_at);
CREATE INDEX idx_players_composite ON mg_players(rank, last_seen);

-- Show table information
SELECT 
    TABLE_NAME as 'Table',
    TABLE_ROWS as 'Rows',
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) as 'Size (MB)',
    TABLE_COMMENT as 'Description'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'miyotugame'
ORDER BY TABLE_NAME;

-- Database setup complete message
SELECT 'MiyotuGame database schema created successfully!' as 'Status';