TEMPDIR=/Users/jeff/Masterarbeit/Daten/temp/
REMOTEDIR=/home/jeff/Freigabe/Daten/temp/

# Check for proper number of command line arguments
EXPECTED_ARGS=4
E_BADARGS=65

if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` {reference.fasta} {reads.fasta} {newfiles.basename} {additional.params}"
  exit $E_BADARGS
fi

SCRIPTDIR=`dirname $0`
ORIGINALDIR=`dirname $2`

echo "copy files to temporary location: $TEMPDIR"
cp -v $1 $TEMPDIR
cp -v $2 $TEMPDIR
cp -v $SCRIPTDIR/bwa_mapper.sh $TEMPDIR

REMOTE1=`basename $1`
REMOTE2=`basename $2`

echo "connect to remote host"
ssh localhost -p 22222 "cd $REMOTEDIR; ./bwa_mapper.sh $REMOTE1 $REMOTE2 $3 '$4'"

echo "copy mapping file to temporary original location:"
cp -v $TEMPDIR$3.sam $ORIGINALDIR
rm $TEMPDIR$3.sam
rm $TEMPDIR$REMOTE1
rm $TEMPDIR$REMOTE2