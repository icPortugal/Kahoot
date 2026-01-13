# IsKahoot - Jogo Concorrente e Distribuído (PCD 2025/26)

 **IsKahoot** é um projeto desenvolvido para a unidade curricular de Programação Concorrente e Distribuída, consistindo numa versão adaptada e distribuída do jogo popular Kahoot.

## Funcionamento do Jogo
* **Equipas**: O jogo é disputado individualmente, mas os jogadores são organizados em equipas de dois.
* **Pontuação**: A contabilização de pontos e a classificação final são feitas por equipa.
* **Rondas**: Cada ronda envolve o envio de uma pergunta, receção de respostas e atualização de estatísticas.



## Requisitos Técnicos e Concorrência
De acordo com as normas do projeto, não são utilizadas bibliotecas padrão do Java para coordenação, sendo os mecanismos desenvolvidos manualmente:

* **Perguntas Individuais**: Utilização de um `ModifiedCountdownLatch` para gerir bónus de rapidez para os dois primeiros jogadores a responder.
* **Perguntas de Equipa**: Implementação de uma **Barreira** (usando variáveis condicionais) que sincroniza as respostas dos membros da equipa.
* **Gestão de Threads**: O servidor utiliza a thread `DealWithClient` para gerir a interação com cada jogador.
* **Threadpool (Opcional)**: Limitação de concorrência para gerir até cinco jogos em simultâneo.

## Dados e Comunicação
* **Formato de Dados**: As perguntas são lidas de ficheiros JSON através da biblioteca **Gson**.
* **Interface**: O servidor possui uma interface textual (TUI) para criação de jogos, enquanto o cliente utiliza uma interface gráfica (GUI).

## Como Executar

### Lançar o Servidor
-
  ```bash
  java servidor

### Lançar a Equipa
-
  ```bash
  new <num_equipas> <num_jogadores_por_equipa> <num_perguntas>

### Lançar o Cliente
-
  ```bash
  java clienteKahoot {IP} {PORT} {ID_Jogo} {Equipa} {Username}
