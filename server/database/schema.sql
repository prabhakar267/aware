-- phpMyAdmin SQL Dump
-- version 4.0.10.7
-- http://www.phpmyadmin.net
--
-- Host: localhost:3306
-- Generation Time: Dec 11, 2016 at 05:16 AM
-- Server version: 5.6.22-72.0-log
-- PHP Version: 5.4.31

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `csinseew_clean_air`
--
CREATE DATABASE IF NOT EXISTS `csinseew_clean_air` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `csinseew_clean_air`;

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

DROP TABLE IF EXISTS `messages`;
CREATE TABLE IF NOT EXISTS `messages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message` text NOT NULL,
  `user_id` int(11) NOT NULL,
  `lat` float NOT NULL,
  `lon` float NOT NULL,
  `tags` varchar(500) DEFAULT NULL,
  `channel` varchar(500) NOT NULL,
  `score` int(111) NOT NULL DEFAULT '0',
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6 ;

--
-- Dumping data for table `messages`
--

INSERT INTO `messages` (`id`, `message`, `user_id`, `lat`, `lon`, `tags`, `channel`, `score`, `timestamp`) VALUES
(1, 'this is a random message', 1, 28.629, 77.2225, '134', 'random', 2, '2016-12-10 10:31:54'),
(2, 'nsgnng', 7, 28.6288, 77.2223, '25', 'random', -1, '2016-12-10 11:10:43'),
(3, 'dnhndjyd', 7, 28.6288, 77.2223, '12', 'random', 0, '2016-12-10 11:11:03'),
(4, 'khvkgc', 7, 28.6288, 77.2223, '1235', 'random', 0, '2016-12-10 11:18:32'),
(5, 'hey therw', 7, 28.6463, 77.2136, 'None', '', 0, '2016-12-10 22:25:46');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  `score` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=8 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `name`, `score`) VALUES
(1, 'Prabhakar', 0),
(2, 'Prabhakar', 0),
(3, 'Prabhakar', 0),
(4, 'aloo', 0),
(5, 'minion', 0),
(6, 'minion', 0),
(7, 'minion', 0);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
