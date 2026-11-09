# PE-032 — Natureza, meteorologia e estações do ano

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/33

## Organização e vocabulário

A implementação contém 24 novas palavras e 24 perguntas com três escolhas únicas, divididas em duas categorias:
- **Natureza e estações**: Árvore/Tree, Flor/Flower, Rio/River, Montanha/Mountain, Floresta/Forest, Mar/Sea, Praia/Beach, Folha/Leaf, Primavera/Spring, Verão/Summer, Outono/Autumn e Inverno/Winter.
- **Meteorologia**: Sol/Sun, Chuva/Rain, Nuvem/Cloud, Vento/Wind, Neve/Snow, Nevoeiro/Fog, Tempestade/Storm, Arco-íris/Rainbow, Relâmpago/Lightning, Granizo/Hail, Quente/Hot e Frio/Cold.

**Distinções didáticas:** `weather` é o tempo meteorológico, enquanto `time` se refere a horas/tempo cronológico. No inglês britânico, `autumn` é preferido para o outono. `hot` e `cold` são adjetivos usados para descrever a temperatura; «Quente» e «Frio» são as formas de vocabulário de base. No contexto meteorológico, em inglês diz-se `It's hot` / `It's cold`, enquanto em português se diz «Está calor» / «Está frio». As ilustrações são pictogramas e não fotografias nem previsões meteorológicas.

24 ficheiros Android VectorDrawable originais são empacotados offline, sem recursos de terceiros nem tráfego de rede. Os recursos usam IDs estáveis `pe_nature_*` e `pe_weather_*`. A reprodução TTS utiliza o campo `audioText` em en-GB com a voz disponível no dispositivo.

## Testes e limites

```sh
python3 scripts/validate_offline_catalog.py
python3 scripts/validate_category_coverage.py
python3 -m unittest discover -s scripts -p 'test_pe032_nature_weather.py' -v
```

Totais do projeto: **211 palavras**, **187 perguntas**, **12 categorias**. O CI compila os testes instrumentados Android, mas não os executa num dispositivo ou emulador. Antes de distribuir a crianças, validar visualmente os vetores, as ambiguidades das palavras e a pronúncia num dispositivo.
