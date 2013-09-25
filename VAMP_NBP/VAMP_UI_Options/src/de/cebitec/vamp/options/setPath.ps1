Param([Parameter(Mandatory=$true)][string]$Directory,[Parameter(Mandatory=$true)][string]$Rhome)
$wsh = new-object -com wscript.shell;
$path = $wsh.Environment("User").Item("Path");
$R_HOME = $wsh.Environment("User").Item("R_HOME");
if ($path) {$path += ";"+$Directory} else {$path = $Directory}
$wsh.Environment("User").Item("Path") = $path;
if ($R_HOME) {} else {
$wsh.Environment("User").Item("R_HOME") = $Rhome;
}