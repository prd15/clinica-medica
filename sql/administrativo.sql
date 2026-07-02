-- =====================================================================
-- Clinica Medica - Banco: clinica_administrativo
-- =====================================================================
-- Extraido via mysqldump --no-data do banco real (schema gerado
-- automaticamente pelo Hibernate/JPA em runtime via ddl-auto=update,
-- a partir das @Entity do modulo administrativo). Testado do zero
-- num MySQL 8 limpo.
--
-- Tabelas: atendentes, convenios, especialidades, medicos, pacientes
--          + medico_especialidade (tabela de juncao N:N, criada por
--          ultimo pois depende de medicos e especialidades via FK)
--
-- Como usar:
--   mysql -u root -p < sql/administrativo.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS clinica_administrativo
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE clinica_administrativo;

-- dropada primeiro (antes de especialidades/medicos) para reexecucao
-- idempotente do script nao esbarrar nas FKs dela
DROP TABLE IF EXISTS `medico_especialidade`;

DROP TABLE IF EXISTS `atendentes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `atendentes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `ativo` bit(1) NOT NULL,
  `nome` varchar(150) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `usuario` varchar(80) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5i9129rwrgiypep55j0osaloo` (`usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `convenios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `convenios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `ativo` bit(1) NOT NULL,
  `cnpj` varchar(18) NOT NULL,
  `descricao` varchar(500) DEFAULT NULL,
  `nome` varchar(150) NOT NULL,
  `telefone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4ielj2owpy72uouf9e0rqu3vl` (`cnpj`),
  UNIQUE KEY `UKskkcono4q0awcktpi5gkpxfll` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `especialidades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `especialidades` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `descricao` varchar(300) DEFAULT NULL,
  `nome` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjb1x3h2l419rof0qqbx95lvl4` (`nome`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `medicos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `ativo` bit(1) NOT NULL,
  `crm` varchar(20) NOT NULL,
  `nome` varchar(150) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `telefone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrxsweypth0bpyhxpvfd9sfgh3` (`crm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `pacientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pacientes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `convenio_id` bigint DEFAULT NULL,
  `cpf` varchar(14) NOT NULL,
  `data_nascimento` date DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `endereco` varchar(300) DEFAULT NULL,
  `nome` varchar(200) NOT NULL,
  `telefone` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1mj2svx930q0tkx1d18qa9rtf` (`cpf`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- (DROP TABLE ja feito no topo do arquivo, antes de especialidades/medicos)
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medico_especialidade` (
  `medico_id` bigint NOT NULL,
  `especialidade_id` bigint NOT NULL,
  PRIMARY KEY (`medico_id`,`especialidade_id`),
  KEY `FK2a28ewdee6ue3k9dfsupyr4hf` (`especialidade_id`),
  CONSTRAINT `FK2a28ewdee6ue3k9dfsupyr4hf` FOREIGN KEY (`especialidade_id`) REFERENCES `especialidades` (`id`),
  CONSTRAINT `FKba7fdrm2gn3yja3antdhsjp1w` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
