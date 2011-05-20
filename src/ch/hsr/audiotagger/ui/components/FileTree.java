package ch.hsr.audiotagger.ui.components;

import java.awt.Component;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Eine All-In-One-Lšsung zur Anzeige eines FileSystem's in einem {@link JTree}.
 * 
 * @author Manuel Alabor
 */
public class FileTree extends JTree {
	
	protected FileFilter filter = null;
	
	public static final FileFilter DIRECTORY_FILTER = new DirectoryFilter();
	public static final FileFilter ACCEPT_ALL_FILTER = new AcceptAllFilter();
	private static final long serialVersionUID = -1278896618676034099L;
	private static final Comparator<File> FILE_COMPARATOR = new FileComparator();	

	public FileTree() {
		// Home Verzeichnis feststellen
		File homeDir = FileSystemView.getFileSystemView().getHomeDirectory();
		// Wurzelelement erstellen
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(homeDir);
		
//		File[] fsRoots = FileSystemView.getFileSystemView().getRoots();
//		DefaultMutableTreeNode root = new DefaultMutableTreeNode(null);
//		for(File fsRoot: fsRoots) {
//			root.add(new DefaultMutableTreeNode(fsRoot));
//		}
		
		
		filter = DIRECTORY_FILTER;
		
		// Model erstellen mit root als Wurzelelement
		DefaultTreeModel model = new DefaultTreeModel(root);
		setModel(model);
		setShowsRootHandles(true);
		// TreeCellRenderer setzen.
		setCellRenderer(new FileTreeRenderer());
		// Wurzel aufklappen
		expandPath(root);
		// Listener hinzufügen
		addTreeWillExpandListener(new FileTreeWillExpandListener());
		// und so ...
		getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	
	private void expandPath(final DefaultMutableTreeNode parent) {
		parent.removeAllChildren();
		File[] tempf = ((File) parent.getUserObject()).listFiles();

		List<File> files = Arrays.asList(tempf);
		Collections.sort(files, FILE_COMPARATOR);

		DefaultMutableTreeNode temp = null;
		for (File file : files) {
			if (filter != null && !filter.accept(file))
				continue;
			temp = new DefaultMutableTreeNode(file);
			if (file.isDirectory())
				temp.add(new DefaultMutableTreeNode(null));
			parent.add(temp);
		}
		((DefaultTreeModel) getModel()).reload(parent);
	}

	public void setFileFilter(FileFilter fileFilter) {
		filter = fileFilter;
		expandPath((DefaultMutableTreeNode) getModel().getRoot());
	}

	public FileFilter getFileFilter() {
		return filter;
	}

	public File getSelectedFile() {
		TreePath selectionPath = getSelectionPath();
		if (selectionPath == null) return null;
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath
				.getLastPathComponent();
		return (File) node.getUserObject();
	}
	
	// Hilfsklassen ------------------------------------------------------------
	/**
	 * @author Manuel Alabor
	 */
	private class FileTreeWillExpandListener implements TreeWillExpandListener {
		public void treeWillCollapse(TreeExpansionEvent e) {
			((DefaultMutableTreeNode) (e.getPath().getLastPathComponent())).removeAllChildren();
			((DefaultMutableTreeNode) (e.getPath().getLastPathComponent())).add(new DefaultMutableTreeNode(null));
		}

		public void treeWillExpand(TreeExpansionEvent e) {
			expandPath((DefaultMutableTreeNode) (e.getPath().getLastPathComponent()));
		}
	}
	
	/**
	 * Comperator fŸr {@link File}'s.<br/>
	 * Stellt sicher, dass zu Beginn alle Ordner, sortiert nach Namen, stehen,
	 * und anschliessend alle normalen Dateien, ebenfalls sortiert nach Namen,
	 * zu stehen kommen.
	 * 
	 * @author Manuel Alabor
	 */
	private static class FileComparator implements Comparator<File> {
		public int compare(File f1, File f2) {
			if (f1.isDirectory() ^ f2.isDirectory()) {
				return (f1.isDirectory() ? -1 : 1);
			}
			return f1.getName().compareToIgnoreCase(f2.getName());
		}
	}

	/**
	 * Standardfilter, welcher den {@link FileTree} anweist, NUR Ordner im Tree
	 * anzuzeigen.
	 * 
	 * @author Manuel Alabor
	 */
	private static class DirectoryFilter implements FileFilter {
		public boolean accept(File f) {
			return f.isDirectory();
		}
	}

	/**
	 * Standardfilter, welcher den {@link FileTree} anweist, Dateien UND Ordner
	 * im Tree anzuzeigen.
	 * 
	 * @author Manuel Alabor
	 */
	private static class AcceptAllFilter implements FileFilter {
		public boolean accept(File f) {
			return true;
		}
	}
	
	/**
	 * {@link TreeCellRenderer} fŸr das Rendern von Zellen/Entries in einem
	 * {@link FileTree}.
	 * 
	 * @author Manuel Alabor
	 * @see FileTree
	 */
	private class FileTreeRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 7108960088110823163L;
		private final FileSystemView fsv = FileSystemView.getFileSystemView();

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			Object node = ((DefaultMutableTreeNode) value).getUserObject();
			
			if (node instanceof File) {
				File f = (File) node;
				setIcon(fsv.getSystemIcon(f));
				setText(fsv.getSystemDisplayName(f));
			}
			
			return this;
		}
		
	}


}
