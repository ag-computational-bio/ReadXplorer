/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.cebitec.readxplorer.ui.options;


import de.cebitec.readxplorer.api.constants.Paths;
import de.cebitec.readxplorer.api.constants.RServe;
import de.cebitec.readxplorer.utils.Downloader;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.OsUtils;
import de.cebitec.readxplorer.utils.PasswordStore;
import de.cebitec.readxplorer.utils.Unzip;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.prefs.Preferences;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class GnuRPanel extends OptionsPanel implements Observer {
    
    private static final Logger LOG = LoggerFactory.getLogger( GnuRPanel.class.getName() );
    
    private static final long serialVersionUID = 1L;
    
    private final GnuROptionsPanelController controller;
    private final Preferences pref;
    private Downloader downloader;
    private Unzip unzip;
    private File zipFile;
    private boolean passwordChanged = false;
    private final File userDir = Places.getUserDirectory();
    private final File rDir = new File( userDir.getAbsolutePath() + File.separator + "R" );
    private final File versionIndicator = new File( rDir.getAbsolutePath() + File.separator + "rx_minimal_version_2_1" );
    private static final String SOURCE_URI = "R-3.2.0.tar";
    private static final String R_ZIP = "R-3.2.0.zip";
    private static final String DEFAULT_R_DOWNLOAD_MIRROR = "ftp://ftp.cebitec.uni-bielefeld.de/pub/readxplorer_repo/R/";
    private static final String DEFAULT_RSERVE_HOST = "localhost";
    private static final int DEFAULT_RSERVE_PORT = 6311;
    private static final String GNU_LICENSE = "             GNU GENERAL PUBLIC LICENSE\n" +
                                              "               Version 2, June 1991\n" +
                                              "\n" +
                                              " Copyright (C) 1989, 1991 Free Software Foundation, Inc.\n" +
                                              "                       51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA\n" +
                                              " Everyone is permitted to copy and distribute verbatim copies\n" +
                                              " of this license document, but changing it is not allowed.\n" +
                                              "\n" +
                                              "                      Preamble\n" +
                                              "\n" +
                                              "  The licenses for most software are designed to take away your\n" +
                                              "freedom to share and change it.  By contrast, the GNU General Public\n" +
                                              "License is intended to guarantee your freedom to share and change free\n" +
                                              "software--to make sure the software is free for all its users.  This\n" +
                                              "General Public License applies to most of the Free Software\n" +
                                              "Foundation's software and to any other program whose authors commit to\n" +
                                              "using it.  (Some other Free Software Foundation software is covered by\n" +
                                              "the GNU Library General Public License instead.)  You can apply it to\n" +
                                              "your programs, too.\n" +
                                              "\n" +
                                              "  When we speak of free software, we are referring to freedom, not\n" +
                                              "price.  Our General Public Licenses are designed to make sure that you\n" +
                                              "have the freedom to distribute copies of free software (and charge for\n" +
                                              "this service if you wish), that you receive source code or can get it\n" +
                                              "if you want it, that you can change the software or use pieces of it\n" +
                                              "in new free programs; and that you know you can do these things.\n" +
                                              "\n" +
                                              "  To protect your rights, we need to make restrictions that forbid\n" +
                                              "anyone to deny you these rights or to ask you to surrender the rights.\n" +
                                              "These restrictions translate to certain responsibilities for you if you\n" +
                                              "distribute copies of the software, or if you modify it.\n" +
                                              "\n" +
                                              "  For example, if you distribute copies of such a program, whether\n" +
                                              "gratis or for a fee, you must give the recipients all the rights that\n" +
                                              "you have.  You must make sure that they, too, receive or can get the\n" +
                                              "source code.  And you must show them these terms so they know their\n" +
                                              "rights.\n" +
                                              "\n" +
                                              "  We protect your rights with two steps: (1) copyright the software, and\n" +
                                              "(2) offer you this license which gives you legal permission to copy,\n" +
                                              "distribute and/or modify the software.\n" +
                                              "\n" +
                                              "  Also, for each author's protection and ours, we want to make certain\n" +
                                              "that everyone understands that there is no warranty for this free\n" +
                                              "software.  If the software is modified by someone else and passed on, we\n" +
                                              "want its recipients to know that what they have is not the original, so\n" +
                                              "that any problems introduced by others will not reflect on the original\n" +
                                              "authors' reputations.\n" +
                                              "\n" +
                                              "  Finally, any free program is threatened constantly by software\n" +
                                              "patents.  We wish to avoid the danger that redistributors of a free\n" +
                                              "program will individually obtain patent licenses, in effect making the\n" +
                                              "program proprietary.  To prevent this, we have made it clear that any\n" +
                                              "patent must be licensed for everyone's free use or not licensed at all.\n" +
                                              "\n" +
                                              "  The precise terms and conditions for copying, distribution and\n" +
                                              "modification follow.\n" +
                                              "\n" +
                                              "              GNU GENERAL PUBLIC LICENSE\n" +
                                              "   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION\n" +
                                              "\n" +
                                              "  0. This License applies to any program or other work which contains\n" +
                                              "a notice placed by the copyright holder saying it may be distributed\n" +
                                              "under the terms of this General Public License.  The \"Program\", below,\n" +
                                              "refers to any such program or work, and a \"work based on the Program\"\n" +
                                              "means either the Program or any derivative work under copyright law:\n" +
                                              "that is to say, a work containing the Program or a portion of it,\n" +
                                              "either verbatim or with modifications and/or translated into another\n" +
                                              "language.  (Hereinafter, translation is included without limitation in\n" +
                                              "the term \"modification\".)  Each licensee is addressed as \"you\".\n" +
                                              "\n" +
                                              "Activities other than copying, distribution and modification are not\n" +
                                              "covered by this License; they are outside its scope.  The act of\n" +
                                              "running the Program is not restricted, and the output from the Program\n" +
                                              "is covered only if its contents constitute a work based on the\n" +
                                              "Program (independent of having been made by running the Program).\n" +
                                              "Whether that is true depends on what the Program does.\n" +
                                              "\n" +
                                              "  1. You may copy and distribute verbatim copies of the Program's\n" +
                                              "source code as you receive it, in any medium, provided that you\n" +
                                              "conspicuously and appropriately publish on each copy an appropriate\n" +
                                              "copyright notice and disclaimer of warranty; keep intact all the\n" +
                                              "notices that refer to this License and to the absence of any warranty;\n" +
                                              "and give any other recipients of the Program a copy of this License\n" +
                                              "along with the Program.\n" +
                                              "\n" +
                                              "You may charge a fee for the physical act of transferring a copy, and\n" +
                                              "you may at your option offer warranty protection in exchange for a fee.\n" +
                                              "\n" +
                                              "  2. You may modify your copy or copies of the Program or any portion\n" +
                                              "of it, thus forming a work based on the Program, and copy and\n" +
                                              "distribute such modifications or work under the terms of Section 1\n" +
                                              "above, provided that you also meet all of these conditions:\n" +
                                              "\n" +
                                              "    a) You must cause the modified files to carry prominent notices\n" +
                                              "    stating that you changed the files and the date of any change.\n" +
                                              "\n" +
                                              "    b) You must cause any work that you distribute or publish, that in\n" +
                                              "    whole or in part contains or is derived from the Program or any\n" +
                                              "    part thereof, to be licensed as a whole at no charge to all third\n" +
                                              "    parties under the terms of this License.\n" +
                                              "\n" +
                                              "    c) If the modified program normally reads commands interactively\n" +
                                              "    when run, you must cause it, when started running for such\n" +
                                              "    interactive use in the most ordinary way, to print or display an\n" +
                                              "    announcement including an appropriate copyright notice and a\n" +
                                              "    notice that there is no warranty (or else, saying that you provide\n" +
                                              "    a warranty) and that users may redistribute the program under\n" +
                                              "    these conditions, and telling the user how to view a copy of this\n" +
                                              "    License.  (Exception: if the Program itself is interactive but\n" +
                                              "    does not normally print such an announcement, your work based on\n" +
                                              "    the Program is not required to print an announcement.)\n" +
                                              "\n" +
                                              "\n" +
                                              "These requirements apply to the modified work as a whole.  If\n" +
                                              "identifiable sections of that work are not derived from the Program,\n" +
                                              "and can be reasonably considered independent and separate works in\n" +
                                              "themselves, then this License, and its terms, do not apply to those\n" +
                                              "sections when you distribute them as separate works.  But when you\n" +
                                              "distribute the same sections as part of a whole which is a work based\n" +
                                              "on the Program, the distribution of the whole must be on the terms of\n" +
                                              "this License, whose permissions for other licensees extend to the\n" +
                                              "entire whole, and thus to each and every part regardless of who wrote it.\n" +
                                              "\n" +
                                              "Thus, it is not the intent of this section to claim rights or contest\n" +
                                              "your rights to work written entirely by you; rather, the intent is to\n" +
                                              "exercise the right to control the distribution of derivative or\n" +
                                              "collective works based on the Program.\n" +
                                              "\n" +
                                              "In addition, mere aggregation of another work not based on the Program\n" +
                                              "with the Program (or with a work based on the Program) on a volume of\n" +
                                              "a storage or distribution medium does not bring the other work under\n" +
                                              "the scope of this License.\n" +
                                              "\n" +
                                              "  3. You may copy and distribute the Program (or a work based on it,\n" +
                                              "under Section 2) in object code or executable form under the terms of\n" +
                                              "Sections 1 and 2 above provided that you also do one of the following:\n" +
                                              "\n" +
                                              "    a) Accompany it with the complete corresponding machine-readable\n" +
                                              "    source code, which must be distributed under the terms of Sections\n" +
                                              "    1 and 2 above on a medium customarily used for software interchange; or,\n" +
                                              "\n" +
                                              "    b) Accompany it with a written offer, valid for at least three\n" +
                                              "    years, to give any third party, for a charge no more than your\n" +
                                              "    cost of physically performing source distribution, a complete\n" +
                                              "    machine-readable copy of the corresponding source code, to be\n" +
                                              "    distributed under the terms of Sections 1 and 2 above on a medium\n" +
                                              "    customarily used for software interchange; or,\n" +
                                              "\n" +
                                              "    c) Accompany it with the information you received as to the offer\n" +
                                              "    to distribute corresponding source code.  (This alternative is\n" +
                                              "    allowed only for noncommercial distribution and only if you\n" +
                                              "    received the program in object code or executable form with such\n" +
                                              "    an offer, in accord with Subsection b above.)\n" +
                                              "\n" +
                                              "The source code for a work means the preferred form of the work for\n" +
                                              "making modifications to it.  For an executable work, complete source\n" +
                                              "code means all the source code for all modules it contains, plus any\n" +
                                              "associated interface definition files, plus the scripts used to\n" +
                                              "control compilation and installation of the executable.  However, as a\n" +
                                              "special exception, the source code distributed need not include\n" +
                                              "anything that is normally distributed (in either source or binary\n" +
                                              "form) with the major components (compiler, kernel, and so on) of the\n" +
                                              "operating system on which the executable runs, unless that component\n" +
                                              "itself accompanies the executable.\n" +
                                              "\n" +
                                              "If distribution of executable or object code is made by offering\n" +
                                              "access to copy from a designated place, then offering equivalent\n" +
                                              "access to copy the source code from the same place counts as\n" +
                                              "distribution of the source code, even though third parties are not\n" +
                                              "compelled to copy the source along with the object code.\n" +
                                              "\n" +
                                              "\n" +
                                              "  4. You may not copy, modify, sublicense, or distribute the Program\n" +
                                              "except as expressly provided under this License.  Any attempt\n" +
                                              "otherwise to copy, modify, sublicense or distribute the Program is\n" +
                                              "void, and will automatically terminate your rights under this License.\n" +
                                              "However, parties who have received copies, or rights, from you under\n" +
                                              "this License will not have their licenses terminated so long as such\n" +
                                              "parties remain in full compliance.\n" +
                                              "\n" +
                                              "  5. You are not required to accept this License, since you have not\n" +
                                              "signed it.  However, nothing else grants you permission to modify or\n" +
                                              "distribute the Program or its derivative works.  These actions are\n" +
                                              "prohibited by law if you do not accept this License.  Therefore, by\n" +
                                              "modifying or distributing the Program (or any work based on the\n" +
                                              "Program), you indicate your acceptance of this License to do so, and\n" +
                                              "all its terms and conditions for copying, distributing or modifying\n" +
                                              "the Program or works based on it.\n" +
                                              "\n" +
                                              "  6. Each time you redistribute the Program (or any work based on the\n" +
                                              "Program), the recipient automatically receives a license from the\n" +
                                              "original licensor to copy, distribute or modify the Program subject to\n" +
                                              "these terms and conditions.  You may not impose any further\n" +
                                              "restrictions on the recipients' exercise of the rights granted herein.\n" +
                                              "You are not responsible for enforcing compliance by third parties to\n" +
                                              "this License.\n" +
                                              "\n" +
                                              "  7. If, as a consequence of a court judgment or allegation of patent\n" +
                                              "infringement or for any other reason (not limited to patent issues),\n" +
                                              "conditions are imposed on you (whether by court order, agreement or\n" +
                                              "otherwise) that contradict the conditions of this License, they do not\n" +
                                              "excuse you from the conditions of this License.  If you cannot\n" +
                                              "distribute so as to satisfy simultaneously your obligations under this\n" +
                                              "License and any other pertinent obligations, then as a consequence you\n" +
                                              "may not distribute the Program at all.  For example, if a patent\n" +
                                              "license would not permit royalty-free redistribution of the Program by\n" +
                                              "all those who receive copies directly or indirectly through you, then\n" +
                                              "the only way you could satisfy both it and this License would be to\n" +
                                              "refrain entirely from distribution of the Program.\n" +
                                              "\n" +
                                              "If any portion of this section is held invalid or unenforceable under\n" +
                                              "any particular circumstance, the balance of the section is intended to\n" +
                                              "apply and the section as a whole is intended to apply in other\n" +
                                              "circumstances.\n" +
                                              "\n" +
                                              "It is not the purpose of this section to induce you to infringe any\n" +
                                              "patents or other property right claims or to contest validity of any\n" +
                                              "such claims; this section has the sole purpose of protecting the\n" +
                                              "integrity of the free software distribution system, which is\n" +
                                              "implemented by public license practices.  Many people have made\n" +
                                              "generous contributions to the wide range of software distributed\n" +
                                              "through that system in reliance on consistent application of that\n" +
                                              "system; it is up to the author/donor to decide if he or she is willing\n" +
                                              "to distribute software through any other system and a licensee cannot\n" +
                                              "impose that choice.\n" +
                                              "\n" +
                                              "This section is intended to make thoroughly clear what is believed to\n" +
                                              "be a consequence of the rest of this License.\n" +
                                              "\n" +
                                              "\n" +
                                              "  8. If the distribution and/or use of the Program is restricted in\n" +
                                              "certain countries either by patents or by copyrighted interfaces, the\n" +
                                              "original copyright holder who places the Program under this License\n" +
                                              "may add an explicit geographical distribution limitation excluding\n" +
                                              "those countries, so that distribution is permitted only in or among\n" +
                                              "countries not thus excluded.  In such case, this License incorporates\n" +
                                              "the limitation as if written in the body of this License.\n" +
                                              "\n" +
                                              "  9. The Free Software Foundation may publish revised and/or new versions\n" +
                                              "of the General Public License from time to time.  Such new versions will\n" +
                                              "be similar in spirit to the present version, but may differ in detail to\n" +
                                              "address new problems or concerns.\n" +
                                              "\n" +
                                              "Each version is given a distinguishing version number.  If the Program\n" +
                                              "specifies a version number of this License which applies to it and \"any\n" +
                                              "later version\", you have the option of following the terms and conditions\n" +
                                              "either of that version or of any later version published by the Free\n" +
                                              "Software Foundation.  If the Program does not specify a version number of\n" +
                                              "this License, you may choose any version ever published by the Free Software\n" +
                                              "Foundation.\n" +
                                              "\n" +
                                              "  10. If you wish to incorporate parts of the Program into other free\n" +
                                              "programs whose distribution conditions are different, write to the author\n" +
                                              "to ask for permission.  For software which is copyrighted by the Free\n" +
                                              "Software Foundation, write to the Free Software Foundation; we sometimes\n" +
                                              "make exceptions for this.  Our decision will be guided by the two goals\n" +
                                              "of preserving the free status of all derivatives of our free software and\n" +
                                              "of promoting the sharing and reuse of software generally.\n" +
                                              "\n" +
                                              "                      NO WARRANTY\n" +
                                              "\n" +
                                              "  11. BECAUSE THE PROGRAM IS LICENSED FREE OF CHARGE, THERE IS NO WARRANTY\n" +
                                              "FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.  EXCEPT WHEN\n" +
                                              "OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES\n" +
                                              "PROVIDE THE PROGRAM \"AS IS\" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED\n" +
                                              "OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\n" +
                                              "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  THE ENTIRE RISK AS\n" +
                                              "TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU.  SHOULD THE\n" +
                                              "PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING,\n" +
                                              "REPAIR OR CORRECTION.\n" +
                                              "\n" +
                                              "  12. IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING\n" +
                                              "WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR\n" +
                                              "REDISTRIBUTE THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES,\n" +
                                              "INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING\n" +
                                              "OUT OF THE USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED\n" +
                                              "TO LOSS OF DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY\n" +
                                              "YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER\n" +
                                              "PROGRAMS), EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE\n" +
                                              "POSSIBILITY OF SUCH DAMAGES.\n" +
                                              "\n" +
                                              "               END OF TERMS AND CONDITIONS\n" +
                                              "\n" +
                                              "     How to Apply These Terms to Your New Programs\n" +
                                              "\n" +
                                              "  If you develop a new program, and you want it to be of the greatest\n" +
                                              "possible use to the public, the best way to achieve this is to make it\n" +
                                              "free software which everyone can redistribute and change under these terms.\n" +
                                              "\n" +
                                              "  To do so, attach the following notices to the program.  It is safest\n" +
                                              "to attach them to the start of each source file to most effectively\n" +
                                              "convey the exclusion of warranty; and each file should have at least\n" +
                                              "the \"copyright\" line and a pointer to where the full notice is found.\n" +
                                              "\n" +
                                              "    <one line to give the program's name and a brief idea of what it does.>\n" +
                                              "    Copyright (C) <year>  <name of author>\n" +
                                              "\n" +
                                              "    This program is free software; you can redistribute it and/or modify\n" +
                                              "    it under the terms of the GNU General Public License as published by\n" +
                                              "    the Free Software Foundation; either version 2 of the License, or\n" +
                                              "    (at your option) any later version.\n" +
                                              "\n" +
                                              "    This program is distributed in the hope that it will be useful,\n" +
                                              "    but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                                              "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                                              "    GNU General Public License for more details.\n" +
                                              "\n" +
                                              "    You should have received a copy of the GNU General Public License\n" +
                                              "    along with this program; if not, write to the Free Software\n" +
                                              "    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA\n" +
                                              "\n" +
                                              "\n" +
                                              "Also add information on how to contact you by electronic and paper mail.\n" +
                                              "\n" +
                                              "If the program is interactive, make it output a short notice like this\n" +
                                              "when it starts in an interactive mode:\n" +
                                              "\n" +
                                              "    Gnomovision version 69, Copyright (C) year name of author\n" +
                                              "    Gnomovision comes with ABSOLUTELY NO WARRANTY; for details type `show w'.\n" +
                                              "    This is free software, and you are welcome to redistribute it\n" +
                                              "    under certain conditions; type `show c' for details.\n" +
                                              "\n" +
                                              "The hypothetical commands `show w' and `show c' should show the appropriate\n" +
                                              "parts of the General Public License.  Of course, the commands you use may\n" +
                                              "be called something other than `show w' and `show c'; they could even be\n" +
                                              "mouse-clicks or menu items--whatever suits your program.\n" +
                                              "\n" +
                                              "You should also get your employer (if you work as a programmer) or your\n" +
                                              "school, if any, to sign a \"copyright disclaimer\" for the program, if\n" +
                                              "necessary.  Here is a sample; alter the names:\n" +
                                              "\n" +
                                              "  Yoyodyne, Inc., hereby disclaims all copyright interest in the program\n" +
                                              "  `Gnomovision' (which makes passes at compilers) written by James Hacker.\n" +
                                              "\n" +
                                              "  <signature of Ty Coon>, 1 April 1989\n" +
                                              "  Ty Coon, President of Vice\n" +
                                              "\n" +
                                              "This General Public License does not permit incorporating your program into\n" +
                                              "proprietary programs.  If your program is a subroutine library, you may\n" +
                                              "consider it more useful to permit linking proprietary applications with the\n" +
                                              "library.  If this is what you want to do, use the GNU Library General\n" +
                                              "Public License instead of this License.";
    
    
    GnuRPanel( GnuROptionsPanelController controller ) {
        this.controller = controller;
        this.pref = NbPreferences.forModule( Object.class );
        initComponents();
        warningMessage.setText( "" );
        String sourceUri = pref.get( Paths.CRAN_MIRROR, DEFAULT_R_DOWNLOAD_MIRROR ) + SOURCE_URI;
        sourceFileTextField.setText( sourceUri );
        jProgressBar1.setMaximum( 100 );
        setUpListener();
        if( OsUtils.isWindows() ) {
            messages.setText( "'Startup Script' is only supported under Linux." );
            startupScriptButton.setEnabled( false );
            if( !versionIndicator.exists() ) {
                installButton.setEnabled( true );
                jProgressBar1.setEnabled( true );
                messages.setText( "" );
            }
        } else {
            File cebitecIndicator = new File( "/vol/readxplorer/R/CeBiTecMode" );
            if( cebitecIndicator.exists() ) {
                messages.setText( "Rserve is already configured correctly for use in CeBiTec" );
                autoButton.setEnabled( false );
                startupScriptButton.setEnabled( false );
                manualButton.setEnabled( false );
                cranMirror.setEnabled( false );
            } else if( OsUtils.isMac() ) {
                messages.setText( "Only change these settings if you do not want to use the bundled GNU R installation." );
                autoButton.setEnabled( false );
                startupScriptButton.setEnabled( false );
                manualButtonSelected();
            } else {
                messages.setText( "Auto installation is only supported under Windows 7, 8 & 10." );
                autoButton.setEnabled( false );
                manualButtonSelected();
            }
        }
        rServePort.setInputVerifier( new PortInputVerifier() );
        rServeStartupScript.setInputVerifier( new ScriptInputVerifier() );
        usernameTextField.setInputVerifier( new UsernameInputVerifier() );
        passwordTextField.setInputVerifier( new PasswordInputVerifier() );
    }
    
    
    private void setUpListener() {
        cranMirror.addKeyListener( new KeyListener() {
            @Override
            public void keyTyped( KeyEvent e ) {
                controller.changed();
            }
            
            
            @Override
            public void keyPressed( KeyEvent e ) {
            }
            
            
            @Override
            public void keyReleased( KeyEvent e ) {
            }
            
            
        } );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        autoOrmanual = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        cranMirror = new javax.swing.JTextField();
        installButton = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        messages = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        sourceFileTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        rServeHost = new javax.swing.JTextField();
        autoButton = new javax.swing.JRadioButton();
        manualButton = new javax.swing.JRadioButton();
        startupScriptButton = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        rServePort = new javax.swing.JTextField();
        warningMessage = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        rServeStartupScript = new javax.swing.JTextField();
        useAuthCheckBox = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        usernameTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JPasswordField();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel1.text_1")); // NOI18N

        cranMirror.setText(org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.cranMirror.text_1")); // NOI18N
        cranMirror.setToolTipText(org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.cranMirror.toolTipText")); // NOI18N
        cranMirror.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cranMirrorKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(installButton, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.installButton.text_1")); // NOI18N
        installButton.setEnabled(false);
        installButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installButtonActionPerformed(evt);
            }
        });

        jProgressBar1.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(messages, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.messages.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel2.text_1")); // NOI18N

        sourceFileTextField.setEditable(false);
        sourceFileTextField.setText(org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.sourceFileTextField.text_1")); // NOI18N
        sourceFileTextField.setBorder(null);
        sourceFileTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                sourceFileTextFieldMouseReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel3.text")); // NOI18N

        rServeHost.setEditable(false);
        rServeHost.setText(org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.rServeHost.text")); // NOI18N

        autoOrmanual.add(autoButton);
        org.openide.awt.Mnemonics.setLocalizedText(autoButton, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.autoButton.text")); // NOI18N
        autoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoButtonActionPerformed(evt);
            }
        });

        autoOrmanual.add(manualButton);
        org.openide.awt.Mnemonics.setLocalizedText(manualButton, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.manualButton.text")); // NOI18N
        manualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualButtonActionPerformed(evt);
            }
        });

        autoOrmanual.add(startupScriptButton);
        org.openide.awt.Mnemonics.setLocalizedText(startupScriptButton, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.startupScriptButton.text")); // NOI18N
        startupScriptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startupScriptButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel5.text")); // NOI18N

        rServePort.setEditable(false);
        rServePort.setText(org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.rServePort.text")); // NOI18N

        warningMessage.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(warningMessage, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.warningMessage.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel6.text")); // NOI18N

        rServeStartupScript.setEditable(false);
        rServeStartupScript.setText(org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.rServeStartupScript.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(useAuthCheckBox, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.useAuthCheckBox.text")); // NOI18N
        useAuthCheckBox.setEnabled(false);
        useAuthCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useAuthCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel7.text")); // NOI18N

        usernameTextField.setEditable(false);
        usernameTextField.setText(org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.usernameTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel8.text")); // NOI18N

        passwordTextField.setEditable(false);
        passwordTextField.setText(org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.passwordTextField.text")); // NOI18N
        passwordTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordTextFieldFocusGained(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cranMirror)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(73, 73, 73))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(autoButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(startupScriptButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(manualButton))
                            .addComponent(jLabel1)
                            .addComponent(installButton)
                            .addComponent(jLabel6)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(useAuthCheckBox))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator1)
                            .addComponent(messages, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sourceFileTextField)
                            .addComponent(jSeparator3)
                            .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(rServeStartupScript)
                            .addComponent(rServeHost)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(rServePort, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(warningMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(passwordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoButton)
                    .addComponent(manualButton)
                    .addComponent(startupScriptButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cranMirror, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(installButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourceFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messages)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rServeStartupScript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rServeHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rServePort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(warningMessage))
                .addGap(18, 18, 18)
                .addComponent(useAuthCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void installButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installButtonActionPerformed
        
        JTextArea textArea = new JTextArea( GNU_LICENSE );
        JScrollPane scrollPane = new JScrollPane( textArea );
        textArea.setLineWrap( true );
        textArea.setWrapStyleWord( true );
        scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
        int showConfirmDialog = JOptionPane.showConfirmDialog( null, scrollPane, "Do you accept the license agreement of GNU R?", JOptionPane.YES_NO_OPTION );
        if( showConfirmDialog == 0 ) {
            try {
                zipFile = File.createTempFile( "ReadXplorer_GNU_R_bundle_", ".zip" );
                zipFile.deleteOnExit();
                String rZip = cranMirror.getText() + R_ZIP;
                downloader = new Downloader( rZip, zipFile );
                downloader.registerObserver( this );
                Thread th = new Thread( downloader );
                th.start();
            } catch( IOException ex ) {
                Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                LOG.error( "{0}: Could not create temporary file.", currentTimestamp );
            }
        }
    }//GEN-LAST:event_installButtonActionPerformed

    private void sourceFileTextFieldMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceFileTextFieldMouseReleased
        if( Desktop.isDesktopSupported() ) {
            Desktop desk = Desktop.getDesktop();
            try {
                desk.browse( new URI( cranMirror.getText() + SOURCE_URI ) );
            } catch( URISyntaxException | IOException ex ) {
                Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
                LOG.warn( "{0}: Could not open URI to GNU R source file.", currentTimestamp );
            }
        }
    }//GEN-LAST:event_sourceFileTextFieldMouseReleased

    private void cranMirrorKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cranMirrorKeyReleased
        this.sourceFileTextField.setText( cranMirror.getText() + SOURCE_URI );
    }//GEN-LAST:event_cranMirrorKeyReleased

    private void manualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualButtonActionPerformed
        manualButtonSelected();
    }//GEN-LAST:event_manualButtonActionPerformed
    
    
    private void manualButtonSelected() {
        rServeStartupScript.setEditable( false );
        rServeHost.setEditable( true );
        rServePort.setEditable( true );
        installButton.setEnabled( false );
        useAuthCheckBox.setEnabled( true );
    }

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
        autoButtonSelected();
    }//GEN-LAST:event_autoButtonActionPerformed
    
    
    private void autoButtonSelected() {
        rServeStartupScript.setEditable( false );
        rServeHost.setEditable( false );
        rServePort.setEditable( false );
        installButton.setEnabled( true );
        warningMessage.setText( "" );
        useAuthCheckBox.setEnabled( false );
        usernameTextField.setEditable( false );
        passwordTextField.setEditable( false );
    }

    private void startupScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startupScriptButtonActionPerformed
        startupScriptButtonSelected();
    }//GEN-LAST:event_startupScriptButtonActionPerformed

    private void useAuthCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useAuthCheckBoxActionPerformed
        useAuthCheckboxSelected();
    }//GEN-LAST:event_useAuthCheckBoxActionPerformed

    private void passwordTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordTextFieldFocusGained
        passwordTextField.setText( "" );
        passwordChanged = true;
    }//GEN-LAST:event_passwordTextFieldFocusGained
    
    
    private void startupScriptButtonSelected() {
        rServeStartupScript.setEditable( true );
        rServeHost.setEditable( false );
        rServePort.setEditable( true );
        installButton.setEnabled( false );
        useAuthCheckBox.setEnabled( true );
    }
    
    
    private void useAuthCheckboxSelected() {
        usernameTextField.setEditable( useAuthCheckBox.isSelected() );
        passwordTextField.setEditable( useAuthCheckBox.isSelected() );
    }
    
    
    @Override
    void load() {
        autoOrmanual.clearSelection();
        cranMirror.setText( pref.get( Paths.CRAN_MIRROR, DEFAULT_R_DOWNLOAD_MIRROR ) );
        rServeHost.setText( pref.get( RServe.RSERVE_HOST, DEFAULT_RSERVE_HOST ) );
        rServePort.setText( String.valueOf( pref.getInt( RServe.RSERVE_PORT, DEFAULT_RSERVE_PORT ) ) );
        boolean manualRemoteButtonSelected = pref.getBoolean( RServe.RSERVE_MANUAL_REMOTE_SETUP, false );
        boolean authSelected = pref.getBoolean( RServe.RSERVE_USE_AUTH, false );
        if( manualRemoteButtonSelected ) {
            autoOrmanual.setSelected( manualButton.getModel(), true );
            manualButtonSelected();
            if( authSelected ) {
                useAuthCheckBox.setSelected( true );
                usernameTextField.setText( pref.get( RServe.RSERVE_USER, "" ) );
                passwordTextField.setText( "xxxxxxxx" );
                useAuthCheckboxSelected();
            }
        } else {
            boolean manualLocalButtonSelected = pref.getBoolean( RServe.RSERVE_MANUAL_LOCAL_SETUP, false );
            if( manualLocalButtonSelected ) {
                autoOrmanual.setSelected( startupScriptButton.getModel(), true );
                rServeStartupScript.setText( pref.get( RServe.RSERVE_STARTUP_SCRIPT, "" ) );
                startupScriptButtonSelected();
                if( authSelected ) {
                    useAuthCheckBox.setSelected( true );
                    usernameTextField.setText( pref.get( RServe.RSERVE_USER, "" ) );
                    passwordTextField.setText( "xxxxxxxx" );
                    useAuthCheckboxSelected();
                }
            } else {
                autoOrmanual.setSelected( autoButton.getModel(), true );
            }
        }
    }
    
    
    @Override
    void store() {
        pref.put( Paths.CRAN_MIRROR, cranMirror.getText() );
        boolean manualRemoteButtonSelected = manualButton.isSelected();
        boolean manualLocalButtonSelected = startupScriptButton.isSelected();
        pref.putBoolean( RServe.RSERVE_MANUAL_REMOTE_SETUP, manualRemoteButtonSelected );
        pref.putBoolean( RServe.RSERVE_MANUAL_LOCAL_SETUP, manualLocalButtonSelected );
        if( manualButton.isSelected() ) {
            pref.put( RServe.RSERVE_HOST, rServeHost.getText() );
            pref.putInt( RServe.RSERVE_PORT, Integer.parseInt( rServePort.getText() ) );
            pref.remove( RServe.RSERVE_STARTUP_SCRIPT );
            if( useAuthCheckBox.isSelected() ) {
                pref.putBoolean( RServe.RSERVE_USE_AUTH, true );
                pref.put( RServe.RSERVE_USER, usernameTextField.getText() );
                if( passwordChanged ) {
                    PasswordStore.save( RServe.RSERVE_PASSWORD, passwordTextField.getPassword(), "" );
                    passwordChanged = false;
                }
            } else {
                pref.remove( RServe.RSERVE_USER );
                PasswordStore.delete( RServe.RSERVE_PASSWORD );
            }
        } else if( startupScriptButton.isSelected() ) {
            pref.put( RServe.RSERVE_STARTUP_SCRIPT, rServeStartupScript.getText() );
            pref.putInt( RServe.RSERVE_PORT, Integer.parseInt( rServePort.getText() ) );
            pref.remove( RServe.RSERVE_HOST );
            if( useAuthCheckBox.isSelected() ) {
                pref.putBoolean( RServe.RSERVE_USE_AUTH, true );
                pref.put( RServe.RSERVE_USER, usernameTextField.getText() );
                if( passwordChanged ) {
                    PasswordStore.save( RServe.RSERVE_PASSWORD, passwordTextField.getPassword(), "" );
                    passwordChanged = false;
                }
            } else {
                pref.remove( RServe.RSERVE_USER );
                PasswordStore.delete( RServe.RSERVE_PASSWORD );
            }
        } else {
            pref.remove( RServe.RSERVE_HOST );
            pref.remove( RServe.RSERVE_PORT );
            pref.remove( RServe.RSERVE_STARTUP_SCRIPT );
            pref.remove( RServe.RSERVE_USE_AUTH );
            pref.remove( RServe.RSERVE_USER );
            PasswordStore.delete( RServe.RSERVE_PASSWORD );
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoButton;
    private javax.swing.ButtonGroup autoOrmanual;
    private javax.swing.JTextField cranMirror;
    private javax.swing.JButton installButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JRadioButton manualButton;
    private javax.swing.JLabel messages;
    private javax.swing.JPasswordField passwordTextField;
    private javax.swing.JTextField rServeHost;
    private javax.swing.JTextField rServePort;
    private javax.swing.JTextField rServeStartupScript;
    private javax.swing.JTextField sourceFileTextField;
    private javax.swing.JRadioButton startupScriptButton;
    private javax.swing.JCheckBox useAuthCheckBox;
    private javax.swing.JTextField usernameTextField;
    private javax.swing.JLabel warningMessage;
    // End of variables declaration//GEN-END:variables

    
    private void unzipGNUR() {
        if( !versionIndicator.exists() && rDir.exists() ) {
            rDir.delete();
        }
        rDir.mkdir();
        try {
            unzip = new Unzip( zipFile, rDir );
            unzip.registerObserver( this );
        } catch( Unzip.NoDirectoryException ex ) {
            Exceptions.printStackTrace( ex );
        }
        Thread th = new Thread( unzip );
        th.start();
    }
    
    
    @Override
    public void update( Object args ) {
        if( args instanceof Downloader.Status ) {
            Downloader.Status status = (Downloader.Status) args;
            switch( status ) {
                case FAILED:
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            jProgressBar1.setIndeterminate( false );
                            jProgressBar1.setValue( 0 );
                            messages.setText( "Download failed. Please try again." );
                        }
                        
                        
                    } );
                    downloader.removeObserver( this );
                    break;
                case FINISHED:
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            jProgressBar1.setIndeterminate( false );
                            jProgressBar1.setValue( 100 );
                            messages.setText( "Download finished." );
                        }
                        
                        
                    } );
                    downloader.removeObserver( this );
                    unzipGNUR();
                    break;
                case RUNNING:
                    SwingUtilities.invokeLater( new Runnable() {
                        @Override
                        public void run() {
                            jProgressBar1.setIndeterminate( true );
                            messages.setText( "Downloading GNU R." );
                        }
                        
                        
                    } );
                    break;
                default:
                    LOG.info( "Encountered unknown downloader status." );
            }
        }
        
        if( args instanceof Unzip.Status ) {
            final Unzip.Status status = (Unzip.Status) args;
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    switch( status ) {
                        case FILE_NOT_FOUND:
                            messages.setText( "The user directory does not exist." );
                            jProgressBar1.setIndeterminate( false );
                            jProgressBar1.setValue( 0 );
                            break;
                        case FINISHED:
                            messages.setText( "Setup complete!" );
                            jProgressBar1.setIndeterminate( false );
                            jProgressBar1.setValue( 100 );
                            break;
                        case NO_RIGHTS:
                            messages.setText( "Can not write to user dir. Please check permissions." );
                            jProgressBar1.setIndeterminate( false );
                            jProgressBar1.setValue( 0 );
                            break;
                        case RUNNING:
                            messages.setText( "Extracting GNU R from archive." );
                            jProgressBar1.setIndeterminate( true );
                            break;
                        case FAILED: //fallthrough to default
                        default:
                            messages.setText( "Can not unzip R archive, please try again." );
                            jProgressBar1.setIndeterminate( false );
                            jProgressBar1.setValue( 0 );
                        
                    }
                }
                
                
            } );
        }
        
    }
    
    
    class PortInputVerifier extends InputVerifier {
        
        @Override
        public boolean verify( JComponent input ) {
            JTextField textField = (JTextField) input;
            String text = textField.getText();
            try {
                Integer.parseInt( text );
            } catch( NumberFormatException ex ) {
                warningMessage.setText( "Please enter a valid port number." );
                return false;
            }
            warningMessage.setText( "" );
            return true;
        }
        
        
    }
    
    
    class ScriptInputVerifier extends InputVerifier {
        
        @Override
        public boolean verify( JComponent input ) {
            JTextField textField = (JTextField) input;
            String text = textField.getText();
            File script = new File( text );
            if( script.exists() && script.canExecute() ) {
                warningMessage.setText( "" );
                return true;
            } else if( !startupScriptButton.isSelected() ) {
                warningMessage.setText( "" );
                return true;
            } else {
                warningMessage.setText( "Please enter a valid startup script." );
                return false;
            }
        }
        
        
    }
    
    
    class UsernameInputVerifier extends InputVerifier {
        
        @Override
        public boolean verify( JComponent input ) {
            JTextField textField = (JTextField) input;
            String username = textField.getText();
            if( username.isEmpty() ) {
                warningMessage.setText( "Username cannot be left empty." );
                return false;
            } else if( !manualButton.isSelected() ) {
                warningMessage.setText( "" );
                return true;
            } else {
                warningMessage.setText( "" );
                return true;
            }
        }
        
        
    }
    
    
    class PasswordInputVerifier extends InputVerifier {
        
        @Override
        public boolean verify( JComponent input ) {
            JPasswordField textField = (JPasswordField) input;
            char[] password = textField.getPassword();
            if( password.length > 0 ) {
                warningMessage.setText( "" );
                return true;
            } else if( !manualButton.isSelected() ) {
                warningMessage.setText( "" );
                return true;
            } else {
                warningMessage.setText( "Password cannot be left empty." );
                return false;
            }
        }
        
        
    }
    
}
