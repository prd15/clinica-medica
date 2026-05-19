# Clínica Médica

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Sistema acadêmico de gestão clínica desenvolvido com Java 17, Spring Boot 3 e arquitetura de microsserviços. O projeto organiza as rotinas administrativas, o agendamento de consultas e o atendimento clínico em módulos independentes, cada um com seu próprio banco de dados MySQL.

## Visão Geral

A aplicação foi desenhada para separar responsabilidades de negócio em três serviços principais:

- **Administrativo**: cadastro e manutenção de pacientes, médicos, especialidades, convênios, atendentes e relatórios.
- **Agendamento**: criação, consulta, confirmação, reagendamento e cancelamento de consultas.
- **Atendimento**: registro clínico da consulta, prontuário, anotações, exames e histórico do paciente.

O módulo **commons** centraliza entidades, repositories, services compartilhados, configurações comuns e testes de regra de negócio.
