# 🇬🇧 VCC Brincar ao Inglês

### 🌟 Aprender inglês nunca foi tão divertido!

**VCC Brincar ao Inglês** é uma aplicação educativa para Android, desenvolvida como plugin da **VCCAprender**, destinada a crianças dos **3 aos 8 anos**. O objetivo é ensinar inglês de forma divertida, intuitiva e interativa, através de jogos, imagens, perguntas, desafios e exercícios de pronúncia.

A aplicação utiliza conteúdos em **português de Portugal e inglês**, permitindo às crianças aprender novas palavras, compreender o seu significado e descobrir como são pronunciadas.

## 🎮 Principais funcionalidades

### 🧩 1. Descobre a palavra em inglês

Jogo de perguntas e respostas inspirado no jogo «Magia na ponta dos dedos» da VCCAprender.

A criança visualiza uma pergunta e escolhe a resposta correta entre três opções (A/B/C).

**Exemplos:**

**Pergunta:** Como se escreve «Vermelho» em inglês?

- A — Green
- B — Red ✅
- C — Blue

**Pergunta:** Qual é a palavra inglesa para «Gato»?

- A — Dog
- B — Bird
- C — Cat ✅

O jogo inclui imagens ilustrativas, respostas aleatórias, feedback visual, uma tentativa por pergunta e acompanhamento do progresso.

### 🔊 2. Como se diz em inglês?

Uma atividade dedicada à aprendizagem da pronúncia.

A criança seleciona uma palavra ou expressão em português, visualiza a tradução e carrega num botão para ouvir a pronúncia correta em inglês.

**Exemplos:**

| Português | Inglês |
|---|---|
| Bom dia | Good morning |
| Boa noite | Good night |
| Obrigado | Thank you |
| Por favor | Please |
| Olá | Hello |
| Até amanhã | See you tomorrow |
| Gato | Cat |
| Vermelho | Red |

A reprodução de áudio utiliza uma voz inglesa, permitindo ouvir e repetir as palavras quantas vezes forem necessárias, sem sobreposição de reproduções.

### 📚 3. Categorias de aprendizagem

O jogo poderá incluir várias categorias educativas:

- 🎨 **Cores:** aprender os nomes das cores em inglês.
- 🐾 **Animais:** animais domésticos, selvagens, marinhos e respetivos sons.
- 🔢 **Números:** contar, identificar números e realizar pequenas operações.
- 🔤 **Alfabeto:** letras e palavras associadas.
- 🍎 **Frutas e alimentos:** alimentos do quotidiano.
- 🏠 **Casa:** divisões, móveis e objetos.
- 👕 **Vestuário:** roupas e acessórios.
- 🚗 **Transportes:** veículos terrestres, marítimos e aéreos.
- 👨‍👩‍👧 **Família:** membros da família e relações familiares.
- 🧍 **Corpo humano:** partes do corpo.
- 🌦️ **Natureza:** estações do ano, meteorologia, plantas e flores.
- 📅 **Tempo:** dias da semana, meses e horas.
- 🏫 **Escola:** materiais escolares

---

## Desenvolvimento Android — PE-001

A estrutura inicial do plugin é um **APK independente** da aplicação VCCAprender,
com um identificador de pacote próprio. Esta fase disponibiliza um ecrã inicial
de arranque para confirmar que o projeto compila e abre no Android. A integração
com o Game Plugin SDK v1 e os jogos serão adicionados nas issues PE-003 em diante.

| Componente | Versão |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 8.9 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| Android SDK (compile/target) | 35 |
| Android minSdk | 26 |
| Package/applicationId | com.vcc.aprender.playenglish |
| Versão inicial | 0.1.0 (versionCode 1) |

**Pré-requisitos:** JDK 17 e Android SDK Platform 35 instalados; definir
`ANDROID_HOME` ou criar um `local.properties` (ignorado pelo Git) apontando
para o SDK, por exemplo `sdk.dir=/caminho/para/android-sdk`.

**Linux / macOS / Codespaces:**

```bash
chmod +x ./gradlew
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

**Windows:**

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

**APK:** `app/build/outputs/apk/debug/app-debug.apk`.
Pode ser instalado com `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

O Gradle Wrapper (incluindo `gradle-wrapper.jar`) está incluído e utiliza
a distribuição oficial Gradle 8.9, evitando dependência de uma instalação
global de Gradle. A aplicação é independente e pode ser instalada em paralelo
com `com.vcc.aprender`.

**Nota:** este ecrã de arranque é temporário. Nesta issue, o APK ainda
**não é reconhecido como plugin** pela VCCAprender, pois o contrato,
a Activity exportada e as verificações de assinatura pertencem às
issues PE-003 a PE-006. Não acrescentar o módulo ao projeto principal.

---

## CI — PE-009: gerar APK automaticamente no GitHub

O workflow [Android CI - Debug APK](.github/workflows/android-ci.yml) executa automaticamente em **push para `main`** e nos **Pull Requests para `main`**. Também pode ser iniciado manualmente em **Actions → Android CI - Debug APK → Run workflow**, depois de o workflow existir na branch predefinida.

O pipeline:

1. Configura **JDK 17**, Android SDK Platform **35**, Build Tools **35.0.0** e o Gradle Wrapper do projeto (**8.9**).
2. Executa `./gradlew :app:testDebugUnitTest :app:assembleDebug`.
3. Publica os relatórios de testes no artefacto `VCCAprenderPlayEnglish-test-results-<run_number>`.
4. Em caso de sucesso, publica o APK Debug no artefacto **`VCCAprenderPlayEnglish-debug-<run_number>`**.

**Como descarregar:** abrir **Actions**, selecionar a execução concluída com sucesso, ir à secção **Artifacts**, descarregar o ZIP do APK e extrair `app-debug.apk`. Para instalar no Android, usar `adb install -r app-debug.apk` ou o instalador de APK do dispositivo.

Este APK Debug é apenas para desenvolvimento: **não está assinado com a chave Release do host**. A assinatura de produção será tratada em **PE-008/PE-010**. A descoberta e comunicação com a VCCAprender serão implementadas em **PE-003 a PE-006**.

A CI não publica Releases nem incrementa versões automaticamente nesta etapa.
