# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [1.0.0] - 2026-02-04

### Adicionado
- Sistema de logging estruturado com SLF4J
  - Logs em Storage para rastrear operações de I/O
  - Logs em App para rastrear inicialização
  - Níveis apropriados de log (INFO, DEBUG, WARN, ERROR)
  
- Testes unitários
  - AppSettingsTest com 13 casos de teste
  - StorageTest com 14 casos de teste
  - Cobertura de casos limite e validações
  
- Ícones multiplataforma
  - app.png para Linux (256x256)
  - app.icns para macOS
  - Suporte completo a HiDPI/Retina
  
- Documentação JavaDoc
  - Storage: documentação completa de todos os métodos públicos
  - App: documentação da classe principal
  - AppSettings: documentação de constantes e funcionalidades
  
- Validação robusta de entrada
  - Null checks em Storage.exportTo() e importFrom()
  - Validação de existência de arquivo em importFrom()
  - Verificação de diretórios antes de operações
  
### Melhorado
- Tratamento de exceções
  - IOException adequadamente capturada e logada
  - IllegalArgumentException para parâmetros inválidos
  - FileNotFoundException para arquivos não encontrados
  - SecurityException capturada ao criar diretórios
  
- Constantes em AppSettings
  - MIN_FONT_SIZE, MAX_FONT_SIZE
  - MIN_LINE_SPACING, MAX_LINE_SPACING
  - MIN_ZOOM, MAX_ZOOM
  - MIN_COLOR_STRENGTH, MAX_COLOR_STRENGTH
  - MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT
  
- Mensagens de log informativas
  - Log de criação de diretórios
  - Log de carregamento/salvamento de configurações
  - Log de operações de importação/exportação
  - Log de backups criados
  
- Feedback de erro ao usuário
  - Dialog de erro ao falhar inicialização
  - Mensagens de erro detalhadas
  - Log de erros para diagnóstico
  
### Corrigido
- Permissões do gradlew (agora executável)
- Validação de tamanho mínimo de janela consistente
- Ícones ausentes para Linux e macOS
- Tratamento de null em saveSettings()

### Dependências
- SLF4J 2.0.9 (API + Simple implementation)
- JUnit 5.10.2 com Platform Launcher
- FlatLaf 3.4.1 (mantido)
- FlatLaf IntelliJ Themes 3.4.1 (mantido)

## [0.9.0] - 2026-01-XX

### Inicial
- Interface gráfica com Swing
- Editor com linhas de caderno
- Tema dark/light com FlatLaf
- Zoom configurável
- Salvamento automático
- Sistema de prioridades com cores
- Janela sempre no topo (configurável)
- Atalho Ctrl+S para salvar manual
