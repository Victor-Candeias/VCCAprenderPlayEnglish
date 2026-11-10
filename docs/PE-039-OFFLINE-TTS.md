# PE-039 — Funcionamento offline e voz en-GB indisponível

Issue #40: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/40

## Alterações de implementação

- Catálogo com palavras, perguntas, imagens e traduções fornecido em recursos do APK e verificado automaticamente sem rede.
- Manifest não pede permissões de INTERNET, ACCESS_NETWORK_STATE nem RECORD_AUDIO.
- A síntese da voz continua opcional: o quiz textual não cria nem utiliza motor TTS.
- Após inicialização do motor Android TTS, a aplicação procura **explicitamente** uma voz com locale en-GB, `isNetworkConnectionRequired == false` e sem a flag `KEY_FEATURE_NOT_INSTALLED`. Recusa ausência de voz ou falha em `setVoice`; não aceita substituição silenciosa por voz en-US ou voz cloud.
- Antes de pronunciar, volta a validar a voz ativa; se a seleção offline se perdeu, aborta a leitura e mostra estado indisponível sem iniciar reprodução.
- Ecrã apresenta uma explicação PT-PT e orientação para um adulto instalar a voz offline. Botões de áudio ficam desativados sem voz, e jogos/traduções continuam utilizáveis.
- O `SpeechTurnGate` continua a impedir sobreposições e a impedir desbloqueios por callbacks antigas.

## Verificação automatizada

```sh
python3 scripts/validate_offline_catalog.py
python3 scripts/validate_pe039_offline.py
python3 -m unittest discover -s scripts -p "test_pe039_offline.py" -v
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:compileDebugAndroidTestKotlin
```

O CI verifica a integridade dos conteúdos, ausência de URLs remotos nos JSON, permissões de rede, restrições de voz, testes para condições negativas e compilação Android.

## Checklist manual de aceitação (pendente)

- [ ] Testar APK em **modo avião**: descobrir termos, navegar por categorias, responder e concluir quiz com pontuação
- [ ] Testar em dispositivo **sem voz en-GB instalada**: mensagem PT-PT, botões Ouvir/Repetir desativados, quiz disponível
- [ ] Testar em dispositivo com voz **en-GB offline instalada**: áudio disponível em modo avião, repetição sem sobreposição
- [ ] Testar com voz **en-US apenas** ou voz de rede: sem fallback silencioso
- [ ] Testar alteração e remoção do pacote TTS entre sessões: sem crash
- [ ] Confirmar em instrumento de rede que o motor TTS instalado pelo utilizador não tenta tráfego remoto em modo avião

**Limitação:** os testes estáticos e unitários não substituem a verificação do comportamento real dos diferentes motores TTS Android, nem provam ausência absoluta de tráfego gerado por outro processo do sistema. É necessária validação em dispositivo antes de fechar a PE-039.
