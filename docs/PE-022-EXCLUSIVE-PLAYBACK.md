# PE-022 — Exclusão de fala e desbloqueio real

**Issue:** https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/23

## Comportamento
- A voz en-GB offline é fornecida pela PE-021.
- Ambos os controlos «Ouvir em inglês» e «Repetir pronúncia» partilham a mesma instância de `AndroidEnglishPronunciationOutput`.
- `SpeechTurnGate.tryBegin(id)` reserva a fala antes de chamar `TextToSpeech.speak`. Pedidos adicionais são rejeitados enquanto `isSpeaking` é verdadeiro; o motor usa `QUEUE_ADD`, nunca `QUEUE_FLUSH`.
- Os controlos ficam desativados desde a aceitação do pedido até ao callback do motor: `onDone`, `onStop` ou `onError`.
- A libertação é feita no thread principal por `SpeechTurnGate.finish(utteranceId)` e somente quando o ID corresponde ao pedido ativo. Um callback antigo depois de `stop()` ou de uma nova fala não altera o estado.
- `onError` e falhas síncronas desbloqueiam os controlos, mostram mensagem PT-PT e permitem repetir. Ausência de voz ou falha de inicialização continuam indisponíveis.
- Ao abandonar o ecrã, `close()` invalida o ID e liberta o motor.

## Testes
`SpeechTurnGateTest` (JVM) verifica rejeição de toques duplicados, bloqueio durante reprodução, libertação idempotente, erro/stop e callbacks atrasados.

O CI executa testes JVM, compila `androidTest` e gera APK Debug. A execução dos testes em dispositivo e a verificação do `UtteranceProgressListener` com um motor real continuam necessárias.

## Próxima fase
PE-023 substituirá o texto de demonstração por termos PT→EN reais e navegação do catálogo sem alterar o mecanismo de exclusão.
