# PE-038 — Controlos de som e preferências locais de leitura

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/39

## Funcionalidades
O ecrã de pronúncia apresenta uma secção **Som e leitura**:

- Interruptor de áudio (ativo por omissão), guardado em `SharedPreferences` privadas da aplicação, sem perfil pessoal, rede ou alteração de preferências do host
- Velocidade da voz inglesa britânica: **70–110%**, passos de 5%, predefinição **85%** (PE-021); valores fora dos limites/NaN vindos do armazenamento são normalizados
- Mostrar/ocultar instruções de leitura (visível por omissão), também persistido
- Estado de voz existente (a carregar, pronta, a reproduzir, voz en-GB em falta, erro), acrescido de indicação explícita de som desligado

## Controlo de reprodução

Ao desativar o áudio, o ecrã executa `audio.stop()` **antes** de persistir e apresentar o estado desligado; o `SpeechTurnGate.cancel()` invalida a reprodução corrente e as callbacks tardias. Os botões Ouvir/Repetir são desativados enquanto o áudio estiver desligado, e o handler volta a verificar a preferência antes de invocar TTS. A mudança de velocidade nunca é permitida com áudio em reprodução; se o motor ainda estiver a inicializar, a taxa guardada será aplicada após inicialização.

O TTS mantém o requisito de voz `en-GB` offline e não substitui silenciosamente por `en-US` ou por vozes de rede. O motor continua a ser encerrado no fim do ecrã.

## Verificação

- Testes unitários `ReadingPreferencesTest.kt` verificam defaults e normalização da velocidade
- Teste instrumentado `Pe038AudioPreferencesInstrumentedTest.kt` verifica controlos e bloqueio de playback no ecrã (requer emulador/dispositivo)
- O GitHub Actions executa unit tests e compila AndroidTest; os instrumentados não são executados sem emulador/dispositivo
- Validar em dispositivo: som ativo → iniciar fala → desligar → áudio para imediatamente → reativar → fala funciona sem bloqueio; fechar/abrir o ecrã para confirmar persistência de som, velocidade e instruções

Não são modificadas preferências parentais do VCCAprender, definições de sistema ou dados do perfil da criança.
