-- =====================================================================
-- Clinica Medica - Banco: clinica_atendimento
-- =====================================================================
-- Extraido via mysqldump --no-data do banco real (schema gerado
-- automaticamente pelo Hibernate/JPA em runtime via ddl-auto=update,
-- a partir das @Entity do modulo atendimento). Testado do zero
-- num MySQL 8 limpo.
--
-- Tabelas: atendimentos, prontuarios, anotacoes, solicitacoes_exame
--          + outbox_event (padrao Transactional Outbox)
--
-- Como usar:
--   mysql -u root -p < sql/atendimento.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS clinica_atendimento
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE clinica_atendimento;

DROP TABLE IF EXISTS `anotacoes`;
CREATE TABLE `anotacoes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `data_criacao` datetime(6) DEFAULT NULL,
  `prontuario_id` bigint NOT NULL,
  `texto` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `atendimentos`;
CREATE TABLE `atendimentos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `consulta_id` bigint NOT NULL,
  `data_hora` datetime(6) NOT NULL,
  `medico_id` bigint NOT NULL,
  `paciente_id` bigint NOT NULL,
  `status` enum('EM_ANDAMENTO','REALIZADO') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK76qv7m36eo419fd5hwfevmm4x` (`consulta_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `outbox_event`;
CREATE TABLE `outbox_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `aggregate_id` varchar(255) NOT NULL,
  `aggregate_type` varchar(255) NOT NULL,
  `event_type` varchar(255) NOT NULL,
  `payload` text,
  `processado_em` datetime(6) DEFAULT NULL,
  `status` enum('DESCARTADO','FALHA','PENDENTE','PROCESSADO') NOT NULL,
  `tentativas` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_outbox_status_tentativas` (`status`,`tentativas`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `prontuarios`;
CREATE TABLE `prontuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `atendimento_id` bigint NOT NULL,
  `data_criacao` datetime(6) DEFAULT NULL,
  `descricao` text,
  `diagnostico` varchar(255) DEFAULT NULL,
  `observacoes` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7rco89q457350w5x118ld6ujp` (`atendimento_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `solicitacoes_exame`;
CREATE TABLE `solicitacoes_exame` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `atendimento_id` bigint NOT NULL,
  `data_solicitacao` datetime(6) DEFAULT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `tipo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
