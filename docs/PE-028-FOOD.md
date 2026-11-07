# PE-028 — Frutas, alimentos e bebidas

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/29

## Conteúdo

- 24 palavras no catálogo (12 pré-existentes + 12 novas), 20 perguntas PT-PT → en-GB.
- Novas palavras: Pera/Pear, Uvas/Grapes, Limão/Lemon, Tomate/Tomato, Batata/Potato, Peixe/Fish, Pizza/Pizza, Massa/Pasta, Iogurte/Yoghurt, Sumo/Juice, Sanduíche/Sandwich, Bolo/Cake.
- Os `id` e os dados pré-existentes são preservados.
- Todas as 24 entradas têm referência a recurso `drawable/pe_food_*` offline. Os 12 novos desenhos vetoriais são originais criados para este projeto (sem fontes externas ou licenças de terceiros).
- TTS: `audioText` corresponde ao termo `enGB`; a síntese de voz usa a configuração linguística en-GB já existente na aplicação.
- A elegibilidade por idade é de 3 a 8 anos para os 12 novos itens.
- As perguntas têm três escolhas únicas dentro da categoria food; não há necessidade de rede.

## Validação

`python3 scripts/validate_offline_catalog.py` e
`python3 -m unittest discover -s scripts -p 'test_pe028_food.py' -v`.
Os testes automáticos adicionais cobrem cobertura total, a unicidade de opções, traduções, ligação a recursos offline e existência de `audioText`.

O CI compila os testes Android instrumentados, mas não os executa num dispositivo. A adequação visual/pedagógica dos pictogramas deve ainda ser revista numa instalação real, incluindo os recursos anteriores da PE-036.
