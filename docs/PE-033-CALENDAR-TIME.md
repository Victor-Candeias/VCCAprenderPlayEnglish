# PE-033 — Dias, meses, horas e períodos do dia
Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/34

## Vocabulário e progressão etária
- **Dias:** Monday a Sunday, 7 termos (idade 3–8).
- **Meses:** January a December, 12 termos (idade 6–8).
- **Períodos do dia:** Morning, Afternoon, Evening, Night, 4 termos (idade 3–8).
- **Horas certas:** It's one o'clock a It's eight o'clock, 8 frases (idade 6–8).
- **Cumprimentos:** Good morning, Good afternoon, Good evening e Good night, 4 expressões (idade 3–8).

**Good evening** é um cumprimento à noite; **Good night** é usado para despedir-se ou antes de dormir. Ambas podem traduzir-se por «Boa noite» em português, pelo que o contexto está explícito na etiqueta.

## Estrutura e testes
- Duas categorias: `calendar` (19 termos) e `time` (16 termos).
- São criadas 35 perguntas de PT-PT para en-GB, cada uma com três escolhas distintas dentro do grupo semântico (dias, meses, períodos, horas ou cumprimentos).
- Os 35 vetores Android são originais e incluídos offline no APK. Os ícones de calendário usam índices de 1 a 7 (dia da semana) e de 1 a 12 (mês do ano), não datas de calendário.
- As ilustrações de relógios representam horas certas de 1 a 8.
- TTS a partir de `audioText` en-GB, sujeito à voz instalada localmente.
- Os testes Python validam traduções-chave, contagens, referências offline, opções, progressão etária e correspondência das categorias na UI.
- Totais globais: **246 palavras**, **222 perguntas**, **14 categorias**.

```sh
python3 scripts/validate_offline_catalog.py
python3 scripts/validate_category_coverage.py
python3 -m unittest discover -s scripts -p "test_pe033_calendar_time.py" -v
```

O CI não executa os testes instrumentados Android num dispositivo; compila os testes. Fazer revisão pedagógica das imagens, da interpretação dos numerais de calendário e dos cumprimentos antes da distribuição.
