# PE-035 — Cumprimentos, expressões e pequenas conversas

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/36

## Conteúdos e preservação
- Mantidos os **12 IDs anteriores**, incluindo *Good morning*, *Thank you*, *Please*, *Hello*, *How are you?*, *I'm fine* e *Good night*.
- Acrescentadas **20 expressões PT-PT/en-GB**, com pontuação para TTS e frases completas, incluindo *Good afternoon.*, *Good evening.*, *How are you today?*, *I'm good, thanks.*, *What's your name?*, *My name is Alex.*, *Can I play with you?*, *May I have some water, please?*, *You're welcome.*, *Excuse me.*, *Can you help me?* e *Can you repeat that, please?*
- 20 novas perguntas A/B/C, com opções únicas e validação da categoria de expressões; total da categoria: **32 frases e 28 perguntas**.
- Progressão de 5–8 anos para expressões simples; 6–8 para perguntas e respostas longas.

## Diálogos contextualizados
- `app/src/main/assets/content/dialogues-v1.json` define **10 microdiálogos** offline, por IDs estáveis de `phrases`.
- O ecrã de descoberta apresenta a sequência **A/B** (PT-PT + en-GB) para cumprimentos, bem-estar, apresentação, conhecer alguém, brincar, pedir água, agradecer, pedir ajuda, despedir-se e pedir repetição.
- Apenas diálogos com **ambas** as frases disponíveis para o nível etário atual aparecem na descoberta. Os termos individuais continuam disponíveis em pronúncia TTS.
- `Good evening` é um cumprimento quando encontramos alguém ao fim do dia; `Good night` é sobretudo uma despedida ou expressão antes de dormir. O contexto está expresso no texto de origem.
- Todas as frases estão no APK; não dependem de rede, nem de dados pessoais.

## Validação
```sh
python3 scripts/validate_offline_catalog.py
python3 scripts/validate_category_coverage.py
python3 -m unittest discover -s scripts -p 'test_pe035_greetings_dialogues.py' -v
```

Totais do projeto: **296 palavras/expressões, 272 perguntas, 16 categorias**. Confirmar a naturalidade das traduções em revisão editorial, a pronúncia no dispositivo e a apresentação de diálogos para ecrãs pequenos. O CI compila os testes Android instrumentados, mas não os executa em dispositivo/emulador.
