-- MySQL dump 10.13  Distrib 8.0.27, for Win64 (x86_64)
--
-- Host: localhost    Database: portal-spolecznosciowy
-- ------------------------------------------------------
-- Server version	8.0.27

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `comments`
--
use heroku_895662a9a78685b;

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8 */;
CREATE TABLE `comments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `body` varchar(255) DEFAULT NULL,
  `created` datetime(6) DEFAULT NULL,
  `post_id` int DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `number_of_dislikes` int NOT NULL,
  `number_of_likes` int NOT NULL,
  `updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh4c7lvsc298whoyd4w9ta25cr` (`post_id`),
  KEY `FK8omq0tc18jd43bu5tjh6jvraq` (`user_id`),
  CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKh4c7lvsc298whoyd4w9ta25cr` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
INSERT INTO `comments` VALUES (38,'A jak!','2022-08-30 19:48:12.934129',45,68,0,0,NULL),(39,'Tak trzeba!','2022-08-30 19:50:23.584954',45,68,0,0,NULL),(40,'Nie ma lekko.','2022-08-30 19:51:15.017621',45,68,0,0,NULL),(42,'Ciężko powiedzieć. Chyba open api jest lepsze bo bardziej aktualne.','2022-09-02 11:05:11.472127',46,68,0,0,NULL),(43,'Junit 5 pozwala na korzystanie z dodatkow wprowadzonych w java 8 jak stream czy na puszczenie wielu metod na raz czyli sprawdzenie wątków ;)','2022-09-02 11:06:23.528159',47,68,0,0,NULL),(44,'Redis jest bardziej popularny.','2022-09-02 11:07:47.479770',47,1,0,0,NULL),(45,'Raczej niebieskie* ;)','2022-09-02 11:08:27.881839',49,1,0,0,NULL),(46,'Ja ciąle korzystam z 11 bo i takiej wersji używam w pracy. Ale zastanawiam się nad 17 chociażby ze względu na klasę Records.','2022-09-02 11:09:27.392253',49,1,0,0,NULL),(51,'O tak, to jest kozak!','2022-09-05 14:05:45.486788',63,78,0,0,NULL),(52,'O tak, to jest kozak!','2022-09-05 14:06:56.511126',46,88,0,0,NULL),(53,'Z open api jest ten problem, że nie zczytuje same nazw metod i trzeba to ustawić ręcznie. To z kolei powoduje, że kod się nam trochę rozbudowuje i rozmywa. Jak dla mnie trochę za duża liczba adnotacji wtedy powstaje.','2022-09-05 14:08:22.604976',46,88,0,4,NULL);
/*!40000 ALTER TABLE `comments` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-09-16 10:09:34
