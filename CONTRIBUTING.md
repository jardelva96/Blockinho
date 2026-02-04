# Guia de Contribuição

Obrigado por considerar contribuir com o DockNotas! 🎉

## Como Contribuir

### Reportando Bugs

Se você encontrou um bug, por favor abra uma issue incluindo:

1. **Descrição clara do problema**
2. **Passos para reproduzir**
3. **Comportamento esperado vs. observado**
4. **Ambiente** (SO, versão do Java, etc.)
5. **Screenshots** (se aplicável)

### Sugerindo Melhorias

Para sugerir novas funcionalidades:

1. Verifique se já não existe uma issue similar
2. Descreva claramente a funcionalidade desejada
3. Explique por que ela seria útil
4. Se possível, sugira uma implementação

### Processo de Pull Request

1. **Fork** o repositório
2. **Crie um branch** a partir de `main`:
   ```bash
   git checkout -b feature/minha-funcionalidade
   ```
3. **Faça suas alterações**:
   - Siga o estilo de código existente
   - Adicione testes quando apropriado
   - Atualize documentação se necessário
   - Execute os testes: `./gradlew test`
   - Execute o build: `./gradlew build`

4. **Commit suas mudanças**:
   ```bash
   git commit -m "feat: adiciona funcionalidade X"
   ```
   
   Use prefixos semânticos:
   - `feat:` - Nova funcionalidade
   - `fix:` - Correção de bug
   - `docs:` - Apenas documentação
   - `style:` - Formatação, sem mudança de código
   - `refactor:` - Refatoração de código
   - `test:` - Adição ou correção de testes
   - `chore:` - Tarefas de manutenção

5. **Push para seu fork**:
   ```bash
   git push origin feature/minha-funcionalidade
   ```

6. **Abra um Pull Request**:
   - Descreva suas mudanças claramente
   - Referencie issues relacionadas
   - Aguarde feedback

## Padrões de Código

### Java

- Use **Java 17** features quando apropriado
- Siga convenções de nomenclatura Java:
  - Classes: `PascalCase`
  - Métodos/variáveis: `camelCase`
  - Constantes: `UPPER_SNAKE_CASE`
- Adicione JavaDoc para:
  - Classes públicas
  - Métodos públicos não-triviais
  - Parâmetros complexos
- Mantenha métodos curtos e focados (idealmente < 50 linhas)
- Use logging apropriado:
  ```java
  logger.debug("Mensagem de debug");
  logger.info("Operação completada");
  logger.warn("Situação incomum mas tratável");
  logger.error("Erro que precisa atenção", exception);
  ```

### Testes

- Escreva testes para:
  - Novas funcionalidades
  - Correções de bugs
  - Lógica de negócio complexa
- Use nomes descritivos:
  ```java
  @Test
  void testFontSizeClamping() { ... }
  ```
- Organize com:
  - `@BeforeEach` para setup
  - `@AfterEach` para cleanup
  - Arrange-Act-Assert pattern

### Commits

- Commits devem ser atômicos (uma mudança lógica por commit)
- Mensagens devem ser descritivas
- Primeira linha: sumário (< 72 caracteres)
- Corpo (opcional): detalhes da mudança

Exemplo:
```
feat: adiciona suporte a atalhos customizáveis

- Permite usuário definir atalhos no menu de configurações
- Persiste atalhos em settings.properties
- Adiciona validação para evitar conflitos
- Inclui testes unitários
```

## Estrutura do Projeto

```
src/
├─ main/
│  ├─ java/org/docknotas/
│  │  ├─ App.java              # Classe principal
│  │  ├─ settings/             # Configurações
│  │  ├─ storage/              # Persistência
│  │  └─ ui/                   # Interface gráfica
│  └─ resources/
│     ├─ icons/                # Ícones do app
│     └─ app.properties        # Propriedades
└─ test/
   └─ java/org/docknotas/      # Testes unitários
```

## Desenvolvimento Local

### Requisitos

- JDK 17+
- Gradle (wrapper incluído)
- IDE recomendada: IntelliJ IDEA, Eclipse ou VS Code

### Setup

1. Clone o repositório:
   ```bash
   git clone https://github.com/jardelva96/Blockinho.git
   cd Blockinho
   ```

2. Build o projeto:
   ```bash
   ./gradlew build
   ```

3. Execute em modo desenvolvimento:
   ```bash
   ./gradlew run
   ```

4. Execute os testes:
   ```bash
   ./gradlew test
   ```

### Debug

Para debug, use sua IDE ou:
```bash
./gradlew run --debug-jvm
```

Depois conecte o debugger na porta 5005.

## Código de Conduta

- Seja respeitoso e inclusivo
- Aceite feedback construtivo
- Foque no que é melhor para o projeto
- Mantenha discussões profissionais

## Dúvidas?

- Abra uma issue com a tag `question`
- Envie email para o mantenedor
- Verifique a documentação existente

## Licença

Ao contribuir, você concorda que suas contribuições serão licenciadas sob a mesma licença do projeto.
