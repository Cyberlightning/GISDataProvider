-- MySQL dump 10.13  Distrib 5.5.35, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: 2d3dcapture
-- ------------------------------------------------------
-- Server version	5.5.35-0ubuntu0.12.10.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `2d3dcapture`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `2d3dcapture` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `2d3dcapture`;

--
-- Table structure for table `Imagedata`
--

DROP TABLE IF EXISTS `Imagedata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Imagedata` (
  `imagename` varchar(45) NOT NULL,
  `location` point NOT NULL,
  `altitude` float DEFAULT '0',
  `accuracy` float DEFAULT '0',
  `accelerationgx` float DEFAULT '0',
  `accelerationgy` float DEFAULT NULL,
  `accelerationgz` float DEFAULT '0',
  `rotationalpha` float DEFAULT '0',
  `rotationbeta` float DEFAULT '0',
  `rotationgamma` float DEFAULT '0',
  `screenorientation` float DEFAULT '0',
  `deviceorientation` varchar(10) DEFAULT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `OS` varchar(30) DEFAULT NULL,
  `Browser` varchar(20) DEFAULT NULL,
  `devicetype` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`imagename`),
  SPATIAL KEY `location` (`location`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SubscriptionData`
--

DROP TABLE IF EXISTS `SubscriptionData`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SubscriptionData` (
  `dataid` int(11) NOT NULL AUTO_INCREMENT,
  `subid` int(11) NOT NULL,
  `location` point NOT NULL,
  `pitch` float DEFAULT NULL,
  `yaw` float DEFAULT NULL,
  `roll` float DEFAULT NULL,
  PRIMARY KEY (`dataid`),
  KEY `subid` (`subid`),
  CONSTRAINT `SubscriptionData_ibfk_1` FOREIGN KEY (`subid`) REFERENCES `Subscriptions` (`subid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Subscriptions`
--

DROP TABLE IF EXISTS `Subscriptions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Subscriptions` (
  `subid` int(11) NOT NULL AUTO_INCREMENT,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `subscriptiontype` int(11) NOT NULL,
  PRIMARY KEY (`subid`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-03-11 13:15:59
