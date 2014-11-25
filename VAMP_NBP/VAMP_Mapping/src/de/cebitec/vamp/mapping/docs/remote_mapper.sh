TEMPDIR=/Users/jeff/Masterarbeit/Daten/temp/
REMOTEDIR=/home/jeff/Freigabe/Daten/temp/

# Check for proper number of command line args.
EXPECTED_ARGS=3
E_BADARGS=65

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` {reference.fasta} {reads.fasta} {newfiles.basename}"
  exit $E_BADARGS
fi

echo "copy files to temporary location: $TEMPDIR"
cp -v $1 $TEMPDIR
cp -v $2 $TEMPDIR
cp -v $TEMPDIR../mapper.sh $TEMPDIR

REMOTE1=`basename $1`
REMOTE2=`basename $2`

echo "connect to remote host"
ssh localhost -p 22222 "cd $REMOTEDIR; ./mapper.sh $REMOTE1 $REMOTE2 $3"

echo "copy mapping file to temporary original location:"
cp -v $TEMPDIR$3.sam $TEMPDIR../