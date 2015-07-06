# Check for proper number of command line arguments.
EXPECTED_ARGS=4
E_BADARGS=65

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` {reference.fasta} {reads.fasta} {newfiles.basename} {additional.params}"
  exit $E_BADARGS
fi

ORIGINALDIR=`dirname $2`

echo "map reads from $2 to the reference $1 with params $4"
bwa index $1
bwa aln $4 $1 $2 > $ORIGINALDIR/$3.sai
bwa samse $1 $ORIGINALDIR/$3.sai $2 > $ORIGINALDIR/$3.sam
echo "the result has been saved in $ORIGINALDIR/$3.sam"