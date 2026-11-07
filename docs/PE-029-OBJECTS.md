# PE-029 — Objetos de casa e material escolar

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/30

## Conteúdos
- 24 objetos (12 já existentes, 12 novos) e 20 perguntas de PT-PT para en-GB.
- Novos objetos de casa: Sofá/Sofa, Candeeiro/Lamp, Espelho/Mirror, Frigorífico/Fridge, Prato/Plate, Colher/Spoon.
- Novos objetos escolares: Caderno/Notebook, Régua/Ruler, Borracha/Rubber, Tesoura/Scissors, Mochila escolar/School bag, Secretária/Desk.
- Todas as palavras da categoria têm `subcategory` igual a `home` ou `school`, para mostrar dois grupos na descoberta.
- Todos os 24 objetos têm ilustrações vetoriais originais `drawable/pe_object_*` embutidas no APK. Proveniência: criadas no projeto sem recursos de terceiros; sem obrigações de licença externa.
- As 12 perguntas adicionais têm 3 respostas distintas e o mesmo intervalo etário (4–8). Não se alteram os IDs das 12 palavras e 8 perguntas existentes.
- Pronúncia TTS baseada em `audioText` (en-GB). `rubber` é a grafia britânica para borracha escolar.

## Validação
- `python3 scripts/validate_offline_catalog.py`
- `python3 scripts/validate_category_coverage.py`
- `python3 -m unittest discover -s scripts -p "test_pe029_objects.py" -v`
- O workflow Android CI inclui os testes e compila AndroidTest, mas não executa testes num dispositivo/emulador.
- Recomenda-se revisão visual, pedagógica e de legibilidade a 200% antes de distribuir a crianças.
