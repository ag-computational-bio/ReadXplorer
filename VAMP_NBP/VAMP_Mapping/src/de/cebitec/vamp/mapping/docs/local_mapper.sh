# Check for proper number of command line args.

EXPECTED_ARGS=3
E_BADARGS=65

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` {reference.fasta} {reads.fasta} {newfiles.basename}"
  exit $E_BADARGS
fi


echo "map reads from $2 to the reference $1"
bwa index $1
bwa aln $1 $2 > $3.sai
bwa samse $1 $3.sai $2 > $3.sam
echo "the result has been saved in $3.sam"