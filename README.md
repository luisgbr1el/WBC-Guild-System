# WBC Guild System

Este plugin é uma versão modificada e personalizada do sistema de guildas original, adaptada especificamente para o servidor **WBCSMP**.

## Créditos

- **Código Original:** [chenasyd](https://github.com/chenasyd)
- **Adaptação para WBCSMP:** Equipe de Desenvolvimento WBC

Esta versão removeu sistemas não utilizados pelo servidor (como Economia e Home) e adaptou interfaces e funcionalidades para melhor integração com o gameplay do WBCSMP.

## Funcionalidades Principais

### Gestão de Guildas
- Criação e personalização de guildas (nome, tag, descrição)
- Gerenciamento de membros (convidar, expulsar, promover, rebaixar)
- Sistema de permissões baseado em cargos (Líder, Oficial, Membro)
- Sistema de candidaturas para entrar em guildas

### Sistema de Relações
- Gerenciamento de relações entre guildas (aliado, inimigo, neutro, em guerra, trégua)
- Notificações de status de relacionamento

### Sistema de Níveis
- Progressão de nível da guilda
- Aumento da capacidade de membros conforme o nível

### Interface de Usuário (GUI)
- Interface Gráfica Completa (GUI) para todas as operações
- Menus intuitivos e traduzidos
- Sistema de Status da Guilda integrado ao menu principal

## Comandos

### Jogadores
- `/guild` - Abre o menu principal da guilda
- `/guild create <nome> [tag]` - Cria uma nova guilda
- `/guild invite <jogador>` - Convida um jogador
- `/guild accept <guilda>` - Aceita um convite
- `/guild leave` - Sai da guilda atual
- `/guild info` - Vê informações da guilda
- `/guild list` - Lista todas as guildas

### Administração
- `/guildadmin` - Painel de administração de guildas
- `/guildadmin reload` - Recarrega as configurações

## Configuração

O plugin suporta banco de dados SQLite (padrão) e MySQL para armazenamento de dados.

## Dependências

- **Java 17+**
- **Spigot/Paper/Folia 1.21+**
- **PlaceholderAPI** (Opcional, para variáveis de chat/scoreboard)
