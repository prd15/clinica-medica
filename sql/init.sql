-- =====================================================================
-- Clinica Medica - Cria os 3 bancos de dados de uma vez
-- =====================================================================
-- Atalho que roda administrativo.sql, agendamento.sql e atendimento.sql
-- em sequencia. Os 3 bancos sao independentes entre si (arquitetura
-- database-per-service, sem FK entre bancos diferentes) -- a ordem
-- de execucao entre eles nao importa.
--
-- Como usar (execute a partir da raiz do repositorio):
--   mysql -u root -p < sql/init.sql
-- =====================================================================

SOURCE sql/administrativo.sql;
SOURCE sql/agendamento.sql;
SOURCE sql/atendimento.sql;
