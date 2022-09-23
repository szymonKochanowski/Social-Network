
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
-- Table structure for table `users`
--
use `portal-spolecznosciowy`;

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(64) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'user123','$2a$10$pLDGOIKTjAOt6SeXJBHXdu8PiCAyjOZjmVHMv.aNrne35ItAn.Fc.',1,'ROLE_USER','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTv1vgdYWHDUkyYYYxV4RV78Q4AHDtagK2GRQ&usqp=CAU',NULL,NULL),(3,'user2','$2a$12$6LLG8k2.pl/ZHiOA6G8m4.x3chDTcTj4iHtaZ7bvbNnKtHVg/E9sm',0,'ROLE_USER',NULL,NULL,NULL),(4,'user3','$2a$10$J1REf5oHFjm/ZI0OllibaubRuPNiwAcyNDyGLenCYvKkIBVIrGZDS',1,'ROLE_USER',NULL,NULL,NULL),(8,'karolina','$2a$10$IEe37iFXogR7p8rZNpp5c.LObvTR.9U9GZv2tZeJVa01JdVyoZ6US',1,'ROLE_USER',NULL,NULL,NULL),(46,'geniusz','$2a$10$grJ7t4AkOp1OoBIFg2OiVuRyssyNSzsbZBbbAOcBVE8A9sAlDvfiC',1,'ROLE_USER',NULL,NULL,NULL),(49,'user12A!b','$2a$10$DHlCG8KhMPp5hXM9.SO0u.L.CiSiRq1CNst0shudkYma9R2vPf/Fe',1,'ROLE_USER',NULL,NULL,NULL),(65,'user12A!bc!','$2a$10$4LEEGt9SphAdz0wEw89S3OlPYSo4RD9ohQn2CP32zop2x9c09Tcv.',1,'ROLE_USER',NULL,NULL,NULL),(66,'admin21!Aa','$2a$10$OPYgDhFAT8o4HD4RJSY18ObMJPMxM8vLZevmsvsZR3Rf93SJFXR5u',1,'ROLE_ADMIN','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTv1vgdYWHDUkyYYYxV4RV78Q4AHDtagK2GRQ&usqp=CAU',NULL,'2022-08-30 14:31:40'),(68,'test123!A','$2a$10$9.fY8IveyEwZSdk5wRvX.elW7IgSOP8TtnOx7jZ6jR5qp15mmGAsK',1,'ROLE_USER',NULL,'2022-08-23 18:51:47','2022-08-24 12:22:58'),(69,'admin1!A','$2a$10$Zq7WH/YR9oU3sOPCLb8TXuRf9mNFp.ssCNlm9uFqo2hxH/RYMEUf.',1,'ROLE_ADMIN',NULL,'2022-08-23 18:52:15',NULL),(74,'test12AALL!!!','$2a$10$0ZhpXODjUXYAcDP2oODv9uWORr7DojXE0GmdY6mNDepsjuChDhJiq',1,'ROLE_ADMIN','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTv1vgdYWHDUkyYYYxV4RV78Q4AHDtagK2GRQ&usqp=CAU','2022-08-28 19:25:49','2022-08-28 19:26:28'),(75,'test12AALL2!!!','$2a$10$/SS85sI.INJMyZagwQb3MubtVgeNhaF3iqs5SX0Jt2wITeN.rAIUK',1,'ROLE_ADMIN',NULL,'2022-08-28 19:30:35',NULL),(76,'test12AALL23!!!','$2a$10$YfZX5uGYS3xHTuMnO5wcbeH/xRaUTm5mLJ8VFhmkfdvrpNEfSfF.a',1,'ROLE_USER','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTv1vgdYWHDUkyYYYxV4RV78Q4AHDtagK2GRQ&usqp=CAU','2022-08-28 19:30:57','2022-08-29 10:49:36'),(77,'test12AALL234!!!','$2a$10$jomrtaP9lmCoCsOKv8OLzukK41Q0CU3yicdwj3vFhD7mTdm3tPV0W',1,'ROLE_ADMIN','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTv1vgdYWHDUkyYYYxV4RV78Q4AHDtagK2GRQ&usqp=CAU','2022-08-28 19:34:07',NULL),(78,'test12AALL2345!!!','$2a$10$u12Rfm6VzZLgx1T.hcB70O9RFjhABQdibs2M3BDjXbJiZ2XndCWRC',1,'ROLE_USER','https://cdn-icons-png.flaticon.com/512/149/149071.png','2022-08-28 19:37:30','2022-08-29 13:24:03'),(79,'user12A~#','$2a$10$eRXcR3iRxp2YebTCvpY8B.yQB/9aKjAwcwDWD/EW.bnGSTjNkNAoe',1,'ROLE_USER',NULL,'2022-08-29 15:02:46',NULL),(84,'user12A~a','$2a$10$6fH50Wjmn8/v.dmAohfBe.NwcBu1MmCns6.AdjCktOUvzgUN.IG0O',1,'ROLE_ADMIN',NULL,'2022-09-04 14:41:42','2022-09-04 14:43:21'),(85,'user12A~a2','$2a$10$Vu2J5h2qiwCVssTkJ9Kw6ueFnzT2dvtRgzmZyO9ZhgC6e.j0yE4n2',1,'ROLE_USER',NULL,'2022-09-04 14:48:42','2022-09-04 15:14:26'),(86,'user12A~','$2a$10$4.MMyjdSWdtcWRDvtpRx2uBk8SxJw6eBqlq7FO0h4ez1lgQq8toUG',1,'ROLE_USER',NULL,'2022-09-04 19:55:13',NULL),(87,'user12A~321','$2a$10$4QfCnzr09tOvyB9J7zyKN.nxSo7sEIKInZ4ge4bINmCJKUlw2PksG',1,'ROLE_USER',NULL,'2022-09-05 10:52:51',NULL),(88,'kohan','$2a$10$EL/kwPikb6KpjxGmMUDqxO/UYyiRk.oYYTrnTx4oWPmh22ZN8.MLu',1,'ROLE_USER',NULL,'2022-09-05 13:48:25',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
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
