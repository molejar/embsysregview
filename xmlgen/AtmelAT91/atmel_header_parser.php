<?php
/**
* Config
*/
$INPUT_FILE = "AT91SAM7S256.h";		// Name of file to parse
$OUTPUT_FILE = "AT91SAM7S256.xml";
$MEMORY_SIZE = 4;			// Size of each memory entry
$CHIPNAME = "AT91SAM7S256";		// Name of chip (used for output XML)
$CHIPDESCRIPTION = "";			// Description of chip (used for output XML)

/**
* Program Starts here
*/

// Selected input file
$file_content = file_get_contents( $INPUT_FILE );

$reg_info = array();

// As Atmel organizes everything into structs, lets find them first
$struct_matches = array();
// Find all struct definitions
// Matches: 1 = struct name, 2 = member definitions, 3 = instance name, 4 = pointer name
if( preg_match_all( "/typedef struct ([^{]*) {([^}]*)} ([^,]*), \*([^;]*);/is", $file_content, $struct_matches ) > 0 ) {
  // Now lets find all the base defines
  $base_define_matches = array();
  // Find all base defines
  // Matches: 1 = Base-Name, 2 = struct pointer name, 3 = base address
  if( preg_match_all( "/#define\s+AT91C_BASE_(\S+)\s+\(AT91_CAST\((\S+)\)\s+(0x\S+)\)/i", $file_content, $base_define_matches ) > 0 ) {
    for( $i = 0; $i < count( $base_define_matches[0] ); $i++ ) {
      $struct_index = array_search($base_define_matches[2][$i], $struct_matches[4] );

      // Add basic register entry (as "group" / "registergroup")
      $groupname = $base_define_matches[1][$i];
      // Check if the base name contains an "_", if yes use for registergroup / group splitting
      if( strpos( $groupname, "_" ) !== false ) {
	$groupname_parts = explode( "_", $groupname );
	$groupname = $groupname_parts[0];
	$registergroupname = $groupname_parts[1];
      }
      else {
	$registergroupname = $groupname;
      }
      
      // Setup empty array if not done yet
      if( !isset($reg_info[$groupname]) ) $reg_info[$groupname] = array();
      
      // Now parse all members
      $struct_members = $struct_matches[2][$struct_index];
      $struct_member_matches = array();
      // Find all members of the struct
      // 1 = name of member, 2 = comment / description
      if( preg_match_all( "/\s+AT91_REG\s+([^;]+);\s+\/\/([^\n]*)/is", $struct_members, $struct_member_matches ) > 0 ) {
	echo "Found members for '$groupname' / '$registergroupname' (" . count($struct_member_matches[0]) . "):\n";

	// Now use the base-address and add all members
	$baseaddress = hexdec($base_define_matches[3][$i]);
	$offset = 0;

	// Setup empty array if not done yet
	if( !isset($reg_info[$groupname][$registergroupname]) ) $reg_info[$groupname][$registergroupname] = array();

	for( $j = 0; $j < count($struct_member_matches[0]); $j++ ) {
	  $member_name = $struct_member_matches[1][$j];
	  $member_name_matches = array();
	  $change_offset = 1;
	  echo "Adding entry '$groupname' / '$registergroupname'\n";

	  // Check if the member is an array
	  // 1 = member name, 2 = member size
	  if( preg_match( "/([^\[]+)\[(\d+)\]/i", $member_name, $member_name_matches ) > 0 ) {
	    $member_name = $member_name_matches[1];
	    $change_offset = intval($member_name_matches[2]);
	  }

	  if( $change_offset > 1 ) {
	    for( $k = 0; $k < $change_offset; $k++ ) {
	      $reg_info[$groupname][$registergroupname][] = array( "name" => $member_name . "[" . $k . "]", "description" => trim($struct_member_matches[2][$j]), "address" => sprintf( "0x%X", $baseaddress + ($offset + $k) * $MEMORY_SIZE), "access" => "rw" );
	    }
	  }
	  else {
	    $reg_info[$groupname][$registergroupname][] = array( "name" => $member_name, "description" => trim($struct_member_matches[2][$j]), "address" => sprintf( "0x%X", $baseaddress + $offset * $MEMORY_SIZE), "access" => "rw" );
	  }

	  $offset += $change_offset;
	}

	//var_export( $reg_info );
      }
      else {
	echo "Error finding members for '$groupname'\n";
      }
    }

    //var_export( $reg_info );
    /*foreach($base_define_matches[3] as $match) {
      echo "Found match: " . $match . "\n";
    }*/
  }
  else {
    echo "Error finding defines!\n";
  }

  /*foreach( $matches as $match ) {
    echo "Found match: " . $match[0];
    //var_export( $match );
    echo "\n";
  }*/
}
else {
  echo "Error finding structs!\n";
}

var_export( $reg_info );

// Finally write the register info to an XML sheet
$xmlWriter = new XMLWriter();
//$xmlWriter->openMemory();
$xmlWriter->openURI( $OUTPUT_FILE );
$xmlWriter->setIndent( true );
$xmlWriter->startDocument( "1.0", "UTF-8" );
$xmlWriter->writeDTD( "model", NULL, "embsysregview.dtd" );
$xmlWriter->startElement( "model" );
$xmlWriter->writeAttribute( "chipname", $CHIPNAME );
$xmlWriter->writeElement( "chip_description", $CHIPDESCRIPTION );
foreach( $reg_info as $group => $reg_groups ) {
    $xmlWriter->startElement( "group" );
    $xmlWriter->writeAttribute( "name", $group );
    $xmlWriter->writeAttribute( "description", "" );

    foreach( $reg_groups as $reg_group => $reg_group_members ) {
      $xmlWriter->startElement( "registergroup" );
      $xmlWriter->writeAttribute( "name", $reg_group );
      $xmlWriter->writeAttribute( "description", "" );

      foreach( $reg_group_members as $reg_member ) {
	$xmlWriter->startElement( "register" );
	foreach( $reg_member as $reg_member_info => $reg_member_value ) {
	  $xmlWriter->writeAttribute( $reg_member_info, $reg_member_value );
	}
	$xmlWriter->endElement();
      }
      $xmlWriter->endElement();
    }
    $xmlWriter->endElement();
}
$xmlWriter->endElement();
$xmlWriter->endDocument();
echo $xmlWriter->outputMemory();
