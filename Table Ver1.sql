-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema subweb
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema subweb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `subweb` DEFAULT CHARACTER SET utf8mb3 ;
USE `subweb` ;

-- -----------------------------------------------------
-- Table `subweb`.`account`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `subweb`.`account` (
  `account_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `pwd` VARCHAR(200) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `create_dt` DATE NOT NULL,
  PRIMARY KEY (`account_id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 21
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `subweb`.`subculture`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `subweb`.`subculture` (
  `subculture_id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `genre` VARCHAR(45) NOT NULL,
  `image_path` VARCHAR(1000) NULL DEFAULT NULL,
  `create_dt` DATE NOT NULL,
  PRIMARY KEY (`subculture_id`),
  UNIQUE INDEX `title_UNIQUE` (`title` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 20
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `subweb`.`post`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `subweb`.`post` (
  `post_id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(45) NOT NULL,
  `create_dt` DATE NOT NULL,
  `post_body` MEDIUMTEXT NULL DEFAULT NULL,
  `subculture_id` INT NOT NULL,
  `account_id` INT NULL DEFAULT NULL,
  `is_notice` TINYINT NOT NULL DEFAULT '0',
  PRIMARY KEY (`post_id`),
  INDEX `fk_post_subculture_idx` (`subculture_id` ASC) VISIBLE,
  INDEX `fk_post_account1_idx` (`account_id` ASC) VISIBLE,
  CONSTRAINT `fk_post_account1`
    FOREIGN KEY (`account_id`)
    REFERENCES `subweb`.`account` (`account_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_post_subculture`
    FOREIGN KEY (`subculture_id`)
    REFERENCES `subweb`.`subculture` (`subculture_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 17
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `subweb`.`comment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `subweb`.`comment` (
  `comment_id` INT NOT NULL AUTO_INCREMENT,
  `comment_body` TEXT NULL DEFAULT NULL,
  `create_dt` DATE NOT NULL,
  `account_id` INT NULL DEFAULT NULL,
  `post_id` INT NOT NULL,
  PRIMARY KEY (`comment_id`),
  INDEX `fk_comment_account1_idx` (`account_id` ASC) VISIBLE,
  INDEX `fk_comment_post1_idx` (`post_id` ASC) VISIBLE,
  CONSTRAINT `fk_comment_account1`
    FOREIGN KEY (`account_id`)
    REFERENCES `subweb`.`account` (`account_id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_comment_post1`
    FOREIGN KEY (`post_id`)
    REFERENCES `subweb`.`post` (`post_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 32
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `subweb`.`file`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `subweb`.`file` (
  `file_id` INT NOT NULL AUTO_INCREMENT,
  `file_path` VARCHAR(45) NOT NULL,
  `file_name` VARCHAR(100) NOT NULL,
  `file_size` MEDIUMINT NOT NULL,
  `file_type` VARCHAR(100) NOT NULL,
  `post_id` INT NOT NULL,
  `create_dt` DATE NOT NULL,
  PRIMARY KEY (`file_id`),
  INDEX `fk_file_post1_idx` (`post_id` ASC) VISIBLE,
  CONSTRAINT `fk_file_post1`
    FOREIGN KEY (`post_id`)
    REFERENCES `subweb`.`post` (`post_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `subweb`.`friend_list`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `subweb`.`friend_list` (
  `friend_list_id` INT NOT NULL AUTO_INCREMENT,
  `my_account_id` INT NOT NULL,
  `friend_account_id` INT NOT NULL,
  `create_dt` DATE NOT NULL,
  PRIMARY KEY (`friend_list_id`),
  INDEX `fk_friend_list_accounts1_idx` (`my_account_id` ASC) VISIBLE,
  INDEX `fk_friend_list_accounts2_idx` (`friend_account_id` ASC) VISIBLE,
  CONSTRAINT `fk_friend_list_accounts1`
    FOREIGN KEY (`my_account_id`)
    REFERENCES `subweb`.`account` (`account_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_friend_list_accounts2`
    FOREIGN KEY (`friend_account_id`)
    REFERENCES `subweb`.`account` (`account_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 17
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `subweb`.`message`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `subweb`.`message` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `send_id` INT NULL DEFAULT NULL,
  `receive_id` INT NULL DEFAULT NULL,
  `is_notice` TINYINT NOT NULL,
  `is_request` TINYINT NOT NULL,
  `message_body` MEDIUMTEXT NOT NULL,
  `create_dt` DATE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_message_account_idx` (`send_id` ASC) VISIBLE,
  INDEX `fk_message_account1_idx` (`receive_id` ASC) VISIBLE,
  CONSTRAINT `fk_message_account`
    FOREIGN KEY (`send_id`)
    REFERENCES `subweb`.`account` (`account_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_message_account1`
    FOREIGN KEY (`receive_id`)
    REFERENCES `subweb`.`account` (`account_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 28
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `subweb`.`role`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `subweb`.`role` (
  `role_id` INT NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(45) NOT NULL,
  `account_id` INT NOT NULL,
  PRIMARY KEY (`role_id`),
  INDEX `account_id_idx` (`account_id` ASC) VISIBLE,
  CONSTRAINT `account_id`
    FOREIGN KEY (`account_id`)
    REFERENCES `subweb`.`account` (`account_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 10
DEFAULT CHARACTER SET = utf8mb3;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
