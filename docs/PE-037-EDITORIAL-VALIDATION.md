# PE-037 — Validação editorial PT-PT / en-GB e progressão etária

**Issue:** #38 — https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/38

## Estado da implementação

- **296 entradas e 272 perguntas em 16 categorias** no catálogo de base, acima do mínimo de 150 entradas únicas.
- O novo `scripts/lint_editorial_catalog.py` utiliza os validadores PE-012/013 existentes e acrescenta verificações de correspondência entre `enGB`/`audioText`, Unicode NFC, controlos, traduções equivalentes dentro da categoria, opções com a mesma tradução em PT/en-GB, correspondência dos prompts, adequação de distratores à idade da resposta e IDs estáveis.
- Referências para imagens e XML vetoriais continuam verificadas pelos validadores originais (integração com o linter).
- O CI passa a executar o linter e os testes de regressão para bloquear regressões detetáveis automaticamente.
- O CSV de revisão é gerado **com 296 linhas, todas inicialmente PENDING**. Não se atribui aprovação ou assinatura por máquina.

## Comandos

```sh
python3 scripts/lint_editorial_catalog.py
python3 -m unittest discover -s scripts -p "test_pe037_editorial_catalog.py" -v
python3 scripts/lint_editorial_catalog.py --csv build/pe037-editorial-review.csv
python3 scripts/lint_editorial_catalog.py --review build/pe037-editorial-review.csv
```

O CSV (UTF-8 com BOM para Excel) contém `id,category,ptPT,enGB,audioText,minAge,maxAge,ageBand,imageRef,quizCount,reviewStatus,reviewer,reviewDate,reviewNotes`.

## Checklist de revisão humana (por concluir)

- [ ] Um revisor com conhecimento de PT-PT confirma ortografia, registo, plural/singular e formas inclusivas para cada entrada
- [ ] Um revisor com conhecimento de en-GB confirma naturalidade e tradução correta, incluindo inglês britânico nas expressões idiomáticas
- [ ] São documentados casos com múltiplas traduções possíveis, verificados os três distratores e a ausência de dupla resposta certa por contexto
- [ ] Revisão pedagógica: adequação infantil e progressão 3–5 / 6–8, sem estereótipos
- [ ] Inspeção visual das ilustrações e legibilidade em ecrã pequeno/dispositivo real
- [ ] Revisão de pronúncia en-GB offline em dispositivo, com TTS
- [ ] Todas as linhas do CSV recebem `APPROVED` ou justificação/revisão, com `reviewer`, `reviewDate` e `reviewNotes`
- [ ] Responsável assina a checklist após resolução de observações; só então considerar a validação editorial concluída

**Limite importante:** as verificações determinísticas de texto e IDs **não conseguem certificar** adequação pedagógica, correção semântica de todas as traduções nem legibilidade real das imagens. A checklist e assinaturas permanecem pendentes por decisão humana; não fechar a issue automaticamente antes dessa revisão.
