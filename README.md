# ScadaMY - Open source SCADA
![ScadaMY logo](https://user-images.githubusercontent.com/82009729/128575518-fcef7deb-9be0-4ef9-8946-5c189a6ac411.png)

Você pode baixar a versão mais atual do ScadaMY [aqui](https://github.com/ScadaMY/ScadaMY/releases/latest).

## Sobre
O ScadaMY é um software _open source_ que oferece todas as funcionalidades de um sistema SCADA (Supervisory Control and Data Acquisition, na sigla em inglês) completo. Softwares do tipo SCADA  existem desde o final dos anos 60, e são peça fundamental na automação de processos que envolvam máquinas, controladores lógicos programáveis (CLPs), acionamentos eletrônicos e sensores.

Entre os principais recursos do ScadaMY estão:
- Aquisição de dados em mais de 20 protocolos como: Modbus TCP/IP e Serial, OPC, DNP3, IEC, Serial ASCII e HTTP
- Visualização de dados (variáveis ou "tags") em tempo real
- Construção de telas gráficas (HMI ou sinópticos)
- Engine de Alarmes e Eventos parametrizáveis
- Alarmes sonoros
- Acesso via browser
- Engine de scripts para controle, automação, geração de "receitas", etc.

**O ScadaMY é um software _open source_ e 100% gratuito.** Com o ScadaMY, o único limite é a sua criatividade.

## Instalação
#### Instalação automática
O ScadaMY possui instaladores oficiais para Windows e Linux. Obtenha-os na [página dos lançamentos](https://github.com/ScadaMY/ScadaMY/releases/latest/).

#### Instalação manual
Se você quiser (ou precisar) realizar uma instalação manual, siga estes passos:
- Instale o Java 8 (ou [OpenJDK 8](https://adoptopenjdk.net/releases.html?variant=openjdk8&jvmVariant=hotspot))
- Instale o [Tomcat 9](https://tomcat.apache.org/download-90.cgi)
- Faça o download do [último lançamento](https://github.com/ScadaMY/ScadaMY/releases/latest/)
- Extraia o arquivo `.war` e copie a pasta extraída para dentro da pasta `webapps/`, no Tomcat
- Reinicie o Tomcat

Obs.: O banco de dados usado por padrão é o Derby. Caso você queira utilizar outro banco de dados (como o MySQL/MariaDB) a configuração a ser realizada consiste em editar o arquivo `/WEB-INF/classes/env.properties` e instalar o ConnectorJ referente a seu gerenciador de banco de dados.

#### Requisitos de instalação
O ScadaMY é um software multiplataforma baseado em Java/Tomcat. Qualquer sistema operacional compatível com o Java 8 e Tomcat 9 está apto para rodar o ScadaMY. Note que os requisitos de hardware podem variar dependendo das suas demandas de uso do ScadaMY. Note também que o instalador para Windows suporta apenas a instalação em Windows Vista ou mais recente.

## ScadaMY e Scada-LTS

Em 2016 o ScadaMY se internacionalizou, dando origem ao projeto [Scada-LTS](https://github.com/SCADA-LTS/Scada-LTS). O Scada-LTS é desenvolvido por um time de desenvolvedores de diversos países, e possui um ritmo de desenvolvimento intenso, com diversas melhorias implementadas e novas tecnologias sendo incorporadas à base de código.

Atualmente, as versões do ScadaMY são baseadas no código-fonte da última versão _open source_ do Mango M2M. Em breve, porém, lançaremos a série ScadaMY 2.x tendo como novo núcleo o Scada-LTS.

A tabela abaixo mostra os principais recursos presentes nas diferentes versões do ScadaMY e do Scada-LTS

Recursos   | ScadaMY 1.0 | ScadaMY 1.1 | ScadaMY 1.2 | Scada-LTS
---------- | ----------- | ----------- | ---------- | ---------
Versão do Java | 6 | 7 ou 8 (depende da compilação) | 8 | [11](https://github.com/SCADA-LTS/Scada-LTS/wiki/Scada-LTS-Linux-installation-tutorial)
Versão do Tomcat | 6 | 7 | 9 | [9](https://github.com/SCADA-LTS/Scada-LTS/wiki/Scada-LTS-Linux-installation-tutorial)
Suporte a "perfis de usuário" | Não possui | Possui | Possui | Possui
API REST | Não possui | Não possui | Não possui | Possui
Suporte a Modbus Serial | Sim | Não | Sim | Não (será implementado no futuro)
Instaladores | Windows, Linux (não oficial), instalação manual | Sem instaladores oficiais | Windows, Linux (inclusive Raspberry), instalação manual | Instalação via Docker ou instalação manual

Para conhecer mais sobre o Scada-LTS, visite a [página do projeto no GitHub](https://github.com/SCADA-LTS/Scada-LTS/)

## Suporte
No [Fórum do ScadaMY](forum.scadamy.com.br) você pode se informar, aprender e tirar dúvidas sobre como usar o ScadaMY. Visite também nosso [site](http://www.scadamy.com.br/) e nosso [canal no YouTube](https://www.youtube.com/channel/UC42J9rtRGzqwmj5s3BJZG6Q).

Você ou sua empresa precisam de **suporte comercial para o ScadaMY**? Você pode conferir nosso [curso oficial](http://www.scadamy.com.br/curso-scadamy/) ou entrar em contato com um [integrador do ScadaMY](http://forum.scadamy.com.br/c/integradores).

## Manuais e versões anteriores
Os manuais oficiais do ScadaMY, bem como as versões anteriores do software (1.0 e 1.1), podem ser encontrados [neste repositório](https://github.com/ScadaMY/ScadaBR_Archive).

## Bugs conhecidos
- Você pode enfrentar problemas com o OpenJDK 8 na hora de enviar e-mails. Caso você receba um alarme de erro contendo a mensagem `javax.net.ssl.SSLHandshakeException: No appropriate protocol (protocol is disabled or cipher suites are inappropriate)` edite o arquivo `java.security`, que deve estar em `${JRE}/lib/security/java.security` (no qual `${JRE}` é o caminho para o seu Java Runtime Environment). Neste arquivo, na opção `jdk.tls.disabledAlgorithms` remova `TLSv1` e `TLSv1.1` da lista.
- **O recurso de exportar projeto para um arquivo ZIP não funciona nas versões 1.1 e 1.2 do ScadaMY.** Recomendamos que você exporte as informações do seu projeto como JSON e que arquivos complementares (tais como as imagens de fundo das Representações Gráficas) sejam copiados manualmente.

## Licença
O ScadaMY é um software livre e _open source_, disponibilizado sob [GPL 3+](https://www.gnu.org/licenses/gpl-3.0.en.html).
