use strict;
use warnings;
use Cwd;
use File::Find;
use File::Copy;

if($#ARGV+1 < 2){
	die "Usuage: translator.pl umlInputFilePath hmbProcessingDirectory [verbose=0]\n";
}
my $umlFile = $ARGV[0];
my $hmbDir = $ARGV[1];
my $verbose = $ARGV[2] or 0;
my %umlVarCharMap = ();
my @hmbDirMap = ();

#Parse the UML file
#The key is the identifier
#The value is the size of the varchar field
open (FH, "<",$umlFile) or die $!;
while (my $umlLine = <FH>) {
	if($umlLine =~ m/>(.*) : VARCHAR\((\d+)\)/i){
		$umlVarCharMap{ $1 } = $2;
	}
}
close(FH);

#Prints the %umlVarCharMap hash file
if($verbose){
    print "Found the following: \n\n";
	print "Identifier = Varchar# \n";
	print "--------------------------\n";
	while ( my ($key, $value) = each(%umlVarCharMap) ) {
			print "$key = $value\n";
	}
	print "--------------------------\n\n";
}

#Browses the input directory
#Creates an array @hmbDirMap of all of the files
#that end with the extension hbm.xml
find( {wanted=> \&wanted=>, no_chdir => 1}, $hmbDir );
sub wanted{
	if($_ =~/hbm\.xml$/i){
		push(@hmbDirMap, $_);
	}
}
#Prints the @hmbDirMap array file
if($verbose){
	print "Files found in directory '$hmbDir'\n";
	print "--------------------------\n";
	foreach(@hmbDirMap){
		print("$_\n");
	}
	print "--------------------------\n\n";
}

#Scan through each hbm.xml file in the input directory
#Look for type="string" column="X", where X is a valid key in umlVarCharMap
#Replace type="string" with type="VARCHAR(Y)" where Y is the value of the column key
#Save changes in the same directory with .new appended to the file name
my $linesChanged = 0;
foreach(@hmbDirMap){
	open (FO, ">>","$_.new") or die $!;
	open (FH, "<",$_) or die $!;
	while (my $line = <FH>) {
		if($line =~ m/<.*type="string".*column="(.*)"\/>/i and not ($line =~ /length="\d+"/i) ){
			if($umlVarCharMap{ uc($1) }){ #if the column is found in umlVarCharMap
				my $s1 = "type=\"string\"";
				my $s2 = "type=\"string\" length=\"$umlVarCharMap{uc($1)}\"";
				$line =~ s/$s1/$s2/e;
				$linesChanged++;
				if($verbose){
					print("Found line with column '$1' in umlVarCharMap\n");
					print("\t$line");
				}
				
			}
		}
		print FO $line or die $!;
	}
	close(FH);
	close(FO);
}
if($verbose){
	print("$linesChanged lines changed.\n\n");
}

#Remove the original files, Rename the new files
foreach(@hmbDirMap){
	unlink("$_") or die $!; #move("$_","$_.old") or die $!;
	move("$_.new","$_") or die $!;
}





 


