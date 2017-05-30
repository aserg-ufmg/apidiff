PATH_PROJECT="/home/aline/utilitarios/workspaces/datasetBreakingChanges/survey_whyWeBreakAPIs_projects"

clear;

echo "Process path $PATH_PROJECT"
count=1;
while read line 
do  
	echo ""
	echo ""	
	echo "###################### PROJECT $count -  $line ######################"
	cd $PATH_PROJECT"/"$line
	count=$((count+1));
	git status
done < $1

cd $PATH_PROJECT

