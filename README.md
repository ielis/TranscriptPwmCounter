# TranscriptPwmCounter
Count number of motifs in all transcripts of the human genome.

## How to run
The app depends on using 3S library as well as 3S resources.

### Install libraries
You need to install *3S* library (`v1.2.0`) first which might not be possible if the library has not been yet publicly released.

### Build the app

The app is Maven project, so building is done by running
```bash
cd TranscriptPwmCounter
mvn clean package
``` 

### Run the app
The **input** consists of a file with ENSEMBL gene ids, e.g. 
```text
ENSG00000001460.13
ENSG00000001631.10
ENSG00000002016.12
```

Then we need **3S resources**:
- database
- reference genome FASTA

Paths to resources are provided in `application.properties` file:
```text
# Path to directory with 3S databases & genome FASTA file
threes.data-directory=/path/to/directory
# genome assembly - choose from {hg19, hg38}
threes.genome-assembly=hg19
# Exomiser-like data version
threes.data-version=1902
``` 
These paths are picked up automatically by 3S autoconfiguration.

**Run** the app by

```bash
java -jar counter-cli/target/counter-cli-0.0.1 -c /path/to/application.properties \
score-genes \
/path/to/input.txt \
/path/to/output.csv
```

The output CSV file has structure:
```csv
ENSG,ENST,PWM1,PWM2,...
ENSG1.1,ENST1.1,2,5,...
ENSG1.1,ENST2.6,3,1,...
ENSG2.1,ENST4.4,11,7,...
```

