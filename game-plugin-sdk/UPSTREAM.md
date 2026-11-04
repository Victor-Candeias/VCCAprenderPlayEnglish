# Game Plugin SDK v1 — snapshot fixo da VCCAprender

Este módulo Android Library é uma **cópia imutável de referência** (vendored snapshot) do SDK oficial da aplicação principal. Não depende de uma segunda checkout, de um AAR privado ou do ramo `main` remoto durante a compilação.

- **Origem:** https://github.com/Victor-Candeias/VCCAprender
- **Commit fixado:** `76127ac5902b361c86271037d3943e737f8e35a7`
- **Versão de protocolo:** `GameContract.PROTOCOL_VERSION = 1`
- **SHA Git de `GameContract.kt`:** `5e3f65046fd7979f09786a2a727b4e075de1592c`
- **Licença:** MIT (mesmo autor em ambos os repositórios).
- **Ficheiros replicados sem alterações:** `build.gradle.kts`, `src/main/AndroidManifest.xml`, `src/main/java/com/vcc/aprender/pluginsdk/GameContract.kt`, `src/test/java/com/vcc/aprender/pluginsdk/GameValidationTest.kt`.

**A importação não corresponde a uma atualização automática do host.** O hash da origem é validado pela CI para impedir divergência acidental. A app depende de `implementation(project(":game-plugin-sdk"))`; o SDK é incorporado no APK gerado. Não são criadas Activities exportadas nesta tarefa.

## Atualização controlada do SDK

1. Escolher e rever um commit específico em VCCAprender que continue a suportar o protocolo v1 (não seguir `main` automaticamente).
2. Copiar de forma **literal** os quatro ficheiros do módulo `game-plugin-sdk` desse commit, atualizar o SHA do commit e do blob acima e atualizar o verificador de hash no workflow de CI.
3. Executar `./gradlew :game-plugin-sdk:testDebugUnitTest :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug`; executar testes instrumentados num emulador/telemóvel.
4. Verificar a compatibilidade de todos os campos de `GameContract`/`GameProtocol` e o comportamento real com a versão do host. Se `PROTOCOL_VERSION` mudar ou o wire format quebrar, não fazer upgrade sem migração explícita do host e do plugin.

O **contrato v1** usa uma Activity Android explicitamente endereçada e `Intent` apenas com primitivas, incluindo `gameId`, UUID de sessão, identificador opaco de perfil, versão e contagens de respostas. **A verificação do chamador/certificado, manifesto e lançamento seguro pertencem às issues PE-004 e PE-005**, não a este snapshot.
