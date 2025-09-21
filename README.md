# DockNotas (Blockinho)

Um bloco de notas flutuante, leve e sempre-à-mão (Windows/Mac/Linux).  
Feito em **Java 17 + Swing** com tema moderno **FlatLaf**.

---

## ✨ Recursos
- Editor com **linhas de caderno** (`LineRuledTextArea`)
- Tema **Dark/Light** (FlatLaf)
- **Zoom por %** (slider) + ajuste de **fonte base** e **espaçamento de linhas**
- Borda/faixa com **cor de prioridade** (vermelho, laranja, amarelo, verde, azul, roxo, cinza) e **intensidade**
- **Salvamento automático** do texto e das preferências (tamanho da janela, zoom etc.)
- Janela **sem botões nativos** (undecorated) com grip de redimensionar no canto
- Atalho: **Ctrl+S** para salvar

---

## 📦 Download
Baixe o executável/instalador na aba **Releases**:  
**https://github.com/jardelva96/Blockinho/releases**

- **Windows (recomendado):** `DockNotas.exe` (app-image) ou instalador (`.msi`/`.exe`, se disponível)  
- **Portável:** pasta gerada pelo `jpackage` (`DockNotas/`) com runtime incluído

> **Não precisa de Java instalado** para os pacotes gerados com `jpackage` (runtime embutido).

---

## 🚀 Rodando localmente (dev)

### Pré-requisitos
- **JDK 17** (Temurin/Adoptium recomendado)
- **Gradle Wrapper** já incluso
- (Opcional, para MSI no Windows) **WiX Toolset 3.14+** no `PATH`

### Clonar e compilar
```bash
git clone https://github.com/jardelva96/Blockinho.git
cd Blockinho
./gradlew clean build
Executar no modo dev
./gradlew run
```
🧰 Artefatos de build
1) Fat-JAR (tudo em um)
Gera build/libs/blockinho-<versão>-all.jar:
```bash

./gradlew clean fatJar
```
Executar:
```bash

java -jar build/libs/blockinho-1.0.0-all.jar
```

2) App “portável” (app-image)

Gera build/jpackage/DockNotas/ com executável e runtime:
```bash

./gradlew jpackageImage
```
3) Instalador (MSI/EXE/DMG/DEB)
Windows: MSI requer WiX (candle.exe/light.exe) no PATH.
Sem WiX, gere EXE:

# MSI (com WiX)
```bash

./gradlew jpackageInstaller
```
# EXE (sem WiX)
```bash

./gradlew jpackageInstaller -PinstallerType=exe
```
Artefatos ficam em build/jpackage/.

Comando direto (Windows, EXE sem WiX)
```bash

jpackage `
  --type exe `
  --name DockNotas `
  --vendor Jardel `
  --input build\libs `
  --main-jar blockinho-1.0.0-all.jar `
  --main-class org.docknotas.App `
  --dest build\jpackage `
  --app-version 1.0.0 `
  --icon src\main\resources\icons\app.ico
```
Se um console abrir, use o executável gerado pelo jpackage/instalador (sem console por padrão).

⚙️ Preferências & Persistência
Arquivos salvos em ~/.docknotas/ (Windows: C:\Users\<Você>\.docknotas\):

settings.properties – preferências (tema, fonte, zoom, tamanho…)
```bash

notes.txt – conteúdo das notas

backups/ – cópias manuais

Chaves comuns:

theme: dark | light

fontSize: 10..36

lineSpacing: 12..40

zoomPercent: 50..200

priorityColor: vermelho|laranja|amarelo|verde|azul|roxo|cinza

colorStrengthPercent: 40..100
```
⌨️ Atalhos
```bash

Salvar: Ctrl+S

Zoom: slider inferior ou View → Zoom (%)

Tema: View → Theme

Fonte/Espaçamento: View → Font (base) e Line spacing
```
🧩 Estrutura do projeto
```bash
src/
 ├─ main/
 │   ├─ java/
 │   │   └─ org/docknotas/
 │   │       ├─ App.java
 │   │       ├─ settings/AppSettings.java
 │   │       ├─ storage/Storage.java
 │   │       ├─ ui/
 │   │       │   ├─ BlockinhoFrame.java
 │   │       │   ├─ components/
 │   │       │   │   ├─ DockBar.java
 │   │       │   │   ├─ HeaderBar.java
 │   │       │   │   ├─ LineRuledTextArea.java
 │   │       │   │   └─ ContextMenuFactory.java
 │   │       │   ├─ util/UiTheme.java
 │   │       │   └─ windows/NotesWindow.java
 │   └─ resources/
 │       ├─ app.properties
 │       └─ icons/
 │           ├─ app.ico
 │           └─ docknotas-16/32/48/64/128/256/512.png
```
🐞 Dicas & Troubleshooting
Exe não abre
Use o DockNotas.exe dentro de build/jpackage/DockNotas/ ou o instalador.
Se aparecerem dois processos, feche ambos e execute de novo.

Erro do WiX ao gerar MSI
Instale WiX Toolset 3.14+ e garanta candle.exe/light.exe no PATH.
Alternativa simples: gere EXE com -PinstallerType=exe.

Acentos/encoding na compilação
Garanta UTF-8 no IDE/arquivos. O Gradle já compila em UTF-8.

🤝 Contribuindo
Fork

Branch: git checkout -b feature/minha-ideia

Commit: git commit -m "feat: minha ideia"

Push: git push origin feature/minha-ideia

Abra Pull Request
