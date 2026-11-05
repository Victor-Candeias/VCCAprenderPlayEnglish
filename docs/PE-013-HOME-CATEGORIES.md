# PE-013 — Página inicial e conteúdos de vocabulário por categorias

**Nota de rastreabilidade:** A issue [#14](https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/14)
descreve a PE-013 como um ecrã inicial com dois modos de aprendizagem.
Foi também solicitada uma expansão dos conteúdos por categorias. Esta
implementação cobre ambos, sem antecipar os níveis etários da PE-014.

## Ecrã inicial: dois modos independentes

1. **Descobre a palavra em inglês**: abre um novo ecrã que apresenta as seis
   categorias com o número de palavras por categoria. Ao selecionar uma
   categoria, são mostrados pares PT-PT / EN-GB com letras e texto legíveis.
   O ecrã funciona inteiramente sem Internet e pode navegar até à área
   de pronúncia (a reprodução áudio só será ativada na fase dedicada).
2. **Como se diz em inglês?**: encaminha para o ecrã de perguntas existente.
   Este ecrã continua a ser uma **pré-visualização** até à implementação
   PE-015/020; não inventa pontuações nem resultados.

Os botões estão escritos em PT-PT, têm áreas de toque de pelo menos 56 dp e
identificadores de teste distintos. Não apresenta controlo parental, dados
pessoais, identificadores do perfil ou opções de compras.

O botão **Sair do jogo sem guardar** permanece exclusivamente na
`PlayEnglishPluginActivity` quando aberta pelo host. A saída cancela a
sessão e devolve `RESULT_CANCELED`. A descoberta de palavras não emite
`RESULT_OK`; o pedido original do host permanece guardado na camada
`plugin`, e não é passado para a interface. A navegação no modo autónomo
não envia qualquer resultado ao host.

## Conteúdos expandidos

O asset `app/src/main/assets/content/quiz-v1.json` preserva todos os
identificadores anteriormente lançados na PE-012 e contém agora:

| Categoria | Palavras / expressões | Perguntas |
|---|---:|---:|
| Animais | 12 | 8 |
| Cores | 12 | 8 |
| Números | 12 | 8 |
| Expressões | 12 | 8 |
| Alimentos | 12 | 8 |
| Objetos | 12 | 8 |
| **Total** | **72** | **48** |

Exemplos: `animals.elephant` (Elefante / Elephant),
`colors.grey` (Cinzento / Grey, ortografia britânica),
`phrases.how_are_you` (Como estás? / How are you?) e
`food.strawberry` (Morango / Strawberry).

As perguntas têm três opções distintas, todas pertencentes à categoria
da resposta e com um `correctId` válido. Cada categoria tem oito
perguntas sobre palavras diferentes. Os valores existentes não foram
reordenados nem substituídos; preservam IDs estáveis para o progresso.

**Imagens e áudio:** `imageRef` mantém-se `null`; não existem imagens
ou sons novos nesta fase. `audioText` contém a expressão en-GB a utilizar
pelo motor de pronúncia futuro. A interface não tenta reproduzir ficheiros
que ainda não existem.

## Validações automáticas

- `scripts/validate_offline_catalog.py`: integridade de esquema, idiomas,
  referências, tradução, faixas etárias e ausência de assets remotos.
- `scripts/validate_category_coverage.py`: cobertura de seis categorias,
  pelo menos 12 palavras/8 perguntas por categoria, prompts PT-PT
  correspondentes à resposta, opção da mesma categoria e proibição de
  traduções repetidas na mesma categoria.
- `scripts/test_offline_catalog.py` e `scripts/test_category_coverage.py`:
  testes de adulteração, ausência de categoria e duplicados.
- `CategoryVocabularyUseCaseTest`: teste JVM sem Android para o agrupamento
  e o ViewModel.
- `PlayEnglishNavigationTest`: teste instrumentado de ambos os modos,
  expansão de uma categoria, passagem à pronúncia e regresso ao início.

```bash
python3 scripts/validate_offline_catalog.py
python3 scripts/validate_category_coverage.py
python3 -m unittest discover -s scripts -p "test_offline_catalog.py" -v
python3 -m unittest discover -s scripts -p "test_category_coverage.py" -v
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
```

Os testes instrumentados são compilados em CI mas só executados com
emulador/dispositivo. **PE-014** acrescentará a seleção por categoria e
níveis etários 3–5 / 6–8 especificamente para os jogos.
