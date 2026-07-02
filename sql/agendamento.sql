-- =====================================================================
-- Clinica Medica - Banco: clinica_agendamento
-- =====================================================================
-- Extraido via mysqldump --no-data do banco real (schema gerado
-- automaticamente pelo Hibernate/JPA em runtime via ddl-auto=update,
-- a partir das @Entity do modulo agendamento). Testado do zero
-- num MySQL 8 limpo.
--
-- Tabelas: consultas
-- Observacao: uk_consulta_medico_slot_ativo garante, no nivel de banco,
-- que um medico nao tenha duas consultas ATIVAS no mesmo horario (o
-- campo slot_ativo fica NULL quando a consulta e cancelada, liberando
-- o horario -- multiplos NULL nao colidem em UNIQUE no MySQL).
--
-- Como usar:
--   mysql -u root -p < sql/agendamento.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS clinica_agendamento
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE clinica_agendamento;

DROP TABLE IF EXISTS `consultas`;
CREATE TABLE `consultas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `convenio_id` bigint NOT NULL,
  `data_hora` datetime(6) NOT NULL,
  `medico_id` bigint NOT NULL,
  `observacoes` varchar(500) DEFAULT NULL,
  `paciente_id` bigint NOT NULL,
  `slot_ativo` datetime(6) DEFAULT NULL,
  `status` enum('CANCELADA','CONFIRMADA','PENDENTE','REALIZADA') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_consulta_medico_slot_ativo` (`medico_id`,`slot_ativo`),
  KEY `idx_consulta_medico_data_hora` (`medico_id`,`data_hora`),
  KEY `idx_consulta_paciente` (`paciente_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
