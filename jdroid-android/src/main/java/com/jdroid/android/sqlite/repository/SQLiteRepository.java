package com.jdroid.android.sqlite.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jdroid.android.application.AbstractApplication;
import com.jdroid.android.domain.Entity;
import com.jdroid.android.sqlite.Column;
import com.jdroid.android.sqlite.SQLiteHelper;
import com.jdroid.java.repository.Repository;
import com.jdroid.java.utils.LoggerUtils;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Repository implementation which uses SQLite.
 * 
 * @param <T> Entity type.
 */
public abstract class SQLiteRepository<T extends Entity> implements Repository<T> {
	
	private static final Logger LOGGER = LoggerUtils.getLogger(SQLiteRepository.class);
	
	protected SQLiteHelper dbHelper;
	
	/**
	 * Constructor. It register create SQL statements in {@link SQLiteHelper}.
	 * 
	 * @param dbHelper {@link SQLiteHelper} to be used be repository.
	 */
	public SQLiteRepository(SQLiteHelper dbHelper) {
		this.dbHelper = dbHelper;
		this.dbHelper.addCreateSQL(getCreateTableSQL());
	}
	
	/**
	 * Returns the name of the table which store the entities.
	 * 
	 * @return the table name.
	 */
	protected abstract String getTableName();
	
	/**
	 * Returns the column definitions for the table.
	 * 
	 * @return columns.
	 */
	protected abstract Column[] getColumns();
	
	/**
	 * Returns the name of the column used as id By default it returns {@link Column#ID}.
	 * 
	 * @return the id column name.
	 */
	protected String getIdColumnName() {
		return Column.ID;
	}
	
	/**
	 * Returns the name of the column used as parent id. By default it returns {@link Column#PARENT_ID}.
	 * 
	 * @return the parent id column name.
	 */
	protected String getParentIdColumnName() {
		return Column.PARENT_ID;
	}
	
	/**
	 * Creates and populate an entity instance which data from cursor. It does NOT populate entity children.
	 * 
	 * @param cursor cursor to get data.
	 * @return an entity instance.
	 */
	protected abstract T createObjectFromCursor(Cursor cursor);
	
	/**
	 * Creates and populate an instance of {@link ContentValues} which the entity data.
	 * 
	 * @param item entity to store.
	 * @return the {@link ContentValues} instance.
	 */
	protected abstract ContentValues createContentValuesFromObject(T item);
	
	/**
	 * Called before an entity is stored, allows to store/update entity children.
	 * 
	 * @param item stored entity.
	 */
	protected void onPreStored(T item) {
	}
	
	/**
	 * Called after an entity is stored, allows to store entity children.
	 * 
	 * @param item stored entity.
	 */
	protected void onStored(T item) {
	}

	/**
	 * Called after an entity is updated, allows to store/update entity children.
	 *
	 * @param item stored entity.
	 */
	protected void onUpdated(T item) {
	}

	/**
	 * Called after an entity is loaded. It allows to populate entity children.
	 * 
	 * @param item loaded entity.
	 */
	protected void onLoaded(T item) {
	}
	
	/**
	 * Called after an entity is removed. It allows to remove entity children.
	 * 
	 * @param item removed entity.
	 */
	protected void onRemoved(T item) {
	}
	
	/**
	 * Default sort to be used in ORDER BY section of queries.
	 * 
	 * @return default sort.
	 */
	protected String getDefaultSort() {
		return null;
	}
	
	/**
	 * Begins a transaction if there is not a transaction started yet.
	 * 
	 * @param db Database.
	 * @return true if a transaction has been started.
	 */
	protected boolean beginTransaction(SQLiteDatabase db) {
		boolean endTransaction = false;
		if (!db.inTransaction()) {
			db.beginTransaction();
			endTransaction = true;
			LOGGER.trace("Begin transaction");
		}
		return endTransaction;
	}
	
	/**
	 * Mark current transaction as successful. Only mark the transaction if endTransaction parameter is true.
	 * 
	 * @param db Database.
	 * @param endTransaction Indicates if the transaction should be closed for the current operation.
	 */
	protected void successTransaction(SQLiteDatabase db, boolean endTransaction) {
		if (endTransaction) {
			db.setTransactionSuccessful();
			LOGGER.trace("Success transaction");
		}
	}
	
	/**
	 * Ends current transaction if endTransaction parameter is true.
	 * 
	 * @param db Database.
	 * @param endTransaction Indicates if the transaction should be closed for the current operation.
	 */
	protected void endTransaction(SQLiteDatabase db, boolean endTransaction) {
		if (endTransaction) {
			db.endTransaction();
			LOGGER.trace("End transaction");
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#get(java.lang.Long)
	 */
	@Override
	@SuppressWarnings("resource")
	public T get(Long id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.query(getTableName(), getProjection(), getIdColumnName() + "=?",
				new String[] { id.toString() }, null, null, null);
			T item = null;
			if (cursor.moveToNext()) {
				item = createObjectFromCursor(cursor);
				onLoaded(item);
			}
			LOGGER.trace("Retrieved object from database of type: " + getTableName() + " id: " + id);
			return item;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#findByField(java.lang.String, java.lang.Object[])
	 */
	@SuppressWarnings("resource")
	@Override
	public List<T> findByField(String fieldName, Object... values) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String selection = null;
		String[] selectionArgs = null;
		if ((values != null) && (values.length > 0)) {
			selectionArgs = new String[values.length];
			StringBuilder sb = new StringBuilder(fieldName + " IN (");
			for (int i = 0; i < values.length; i++) {
				selectionArgs[i] = values[i].toString();
				if (i > 0) {
					sb.append(",");
				}
				sb.append("?");
			}
			sb.append(")");
			selection = sb.toString();
		}
		Cursor cursor = null;
		try {
			ArrayList<T> items = new ArrayList<>();
			cursor = db.query(getTableName(), getProjection(), selection, selectionArgs, null, null, getDefaultSort());
			while (cursor.moveToNext()) {
				T item = createObjectFromCursor(cursor);
				onLoaded(item);
				items.add(item);
			}
			cursor.close();
			
			LOGGER.trace("Retrieved objects from database of type: " + getTableName() + " field: " + fieldName
					+ " values: " + values);
			
			return items;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#getAll(java.util.List)
	 */
	@Override
	public List<T> getAll(List<Long> ids) {
		return findByField(getIdColumnName(), ids);
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#getAll()
	 */
	@Override
	public List<T> getAll() {
		List<T> results = findByField(null, new Object[0]);
		LOGGER.trace("Retrieved all objects [" + results.size() + "] from database of type: " + getTableName());
		return results;
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#isEmpty()
	 */
	@Override
	public Boolean isEmpty() {
		return getSize() == 0;
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#getSize()
	 */
	@SuppressWarnings("resource")
	@Override
	public Long getSize() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.query(getTableName(), new String[0], null, null, null, null, null);
			return (long)cursor.getCount();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#add(com.jdroid.java.domain.Identifiable)
	 */
	@Override
	public void add(T item) {
		@SuppressWarnings("resource")
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean endTransaction = beginTransaction(db);
		try {
			ContentValues values = createContentValuesFromObject(item);
			long id = db.insertOrThrow(getTableName(), null, values);
			if (item.getId() == null) {
				item.setId(id);
			}
			onStored(item);
			LOGGER.trace("Stored object in database: " + item);
			successTransaction(db, endTransaction);
		} finally {
			endTransaction(db, endTransaction);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#addAll(java.util.Collection)
	 */
	@Override
	public void addAll(Collection<T> items) {
		@SuppressWarnings("resource")
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean endTransaction = beginTransaction(db);
		try {
			for (T item : items) {
				add(item);
			}
			LOGGER.trace("Stored objects in database:\n" + items);
			successTransaction(db, endTransaction);
		} finally {
			endTransaction(db, endTransaction);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#update(com.jdroid.java.domain.Identifiable)
	 */
	@Override
	public void update(T item) {
		add(item);
		@SuppressWarnings("resource")
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean endTransaction = beginTransaction(db);
		try {
			ContentValues values = createContentValuesFromObject(item);
			db.update(getTableName(), values, getIdColumnName() + "=?", new String[]{item.getId().toString()});
			onUpdated(item);
			LOGGER.trace("Updated object in database: " + item);
			successTransaction(db, endTransaction);
		} finally {
			endTransaction(db, endTransaction);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#replaceAll(java.util.Collection)
	 */
	@Override
	public void replaceAll(Collection<T> items) {
		@SuppressWarnings("resource")
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean endTransaction = beginTransaction(db);
		try {
			removeAll();
			LOGGER.trace("Deleted from database all objects of type: " + getTableName());
			for (T item : items) {
				add(item);
			}
			LOGGER.trace("Stored objects in database:\n" + items);
			successTransaction(db, endTransaction);
		} finally {
			endTransaction(db, endTransaction);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#remove(com.jdroid.java.domain.Identifiable)
	 */
	@Override
	public void remove(T item) {
		remove(item.getId());
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#remove(java.lang.Long)
	 */
	@Override
	public void remove(Long id) {
		@SuppressWarnings("resource")
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean endTransaction = beginTransaction(db);
		try {
			T item = get(id);
			db.delete(getTableName(), getIdColumnName() + "=?", new String[] { id.toString() });
			onRemoved(item);
			LOGGER.trace("Deleted object in database: " + item);
			successTransaction(db, endTransaction);
		} finally {
			endTransaction(db, endTransaction);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#removeAll()
	 */
	@Override
	public void removeAll() {
		@SuppressWarnings("resource")
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean endTransaction = beginTransaction(db);
		try {
			List<T> all = getAll();
			db.delete(getTableName(), null, null);
			for (T item : all) {
				onRemoved(item);
			}
			LOGGER.trace("Deleted from database all objects of type: " + getTableName());
			successTransaction(db, endTransaction);
		} finally {
			endTransaction(db, endTransaction);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#removeAll(java.util.Collection)
	 */
	@Override
	public void removeAll(Collection<T> items) {
		@SuppressWarnings("resource")
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean endTransaction = beginTransaction(db);
		try {
			for (T item : items) {
				remove(item);
			}
			LOGGER.trace("Deleted objects in database: " + items);
			successTransaction(db, endTransaction);
		} finally {
			endTransaction(db, endTransaction);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#getUniqueInstance()
	 */
	@Override
	public T getUniqueInstance() {
		List<T> all = getAll();
		if (all.isEmpty()) {
			return null;
		}
		LOGGER.trace("Retrieved single instance of type: " + getTableName());
		return all.get(0);
	}
	
	/**
	 * This method allows to replace all entity children of a given parent, it will remove any children which are not in
	 * the list, add the new ones and update which are in the list..
	 * 
	 * @param list of children to replace.
	 * @param parentId id of parent entity.
	 */
	public void replaceChildren(List<T> list, Long parentId) {
		for (T item : list) {
			item.setParentId(parentId);
		}
		@SuppressWarnings("resource")
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		boolean endTransaction = beginTransaction(db);
		try {
			db.delete(getTableName(), getParentIdColumnName() + "=?", new String[] { parentId.toString() });
			addAll(list);
			LOGGER.trace("Replaced children of parent " + parentId + " and type " + getTableName());
			successTransaction(db, endTransaction);
		} finally {
			endTransaction(db, endTransaction);
		}
	}
	
	/**
	 * This method allows to replace all entity children of a given parent, it will remove any children which are not in
	 * the list, add the new ones and update which are in the list..
	 * 
	 * @param list of children to replace.
	 * @param parentId id of parent entity.
	 * @param clazz entity class.
	 */
	public static <T extends Entity> void replaceChildren(List<T> list, Long parentId, Class<T> clazz) {
		SQLiteRepository<T> repository = (SQLiteRepository<T>)AbstractApplication.get().getRepositoryInstance(clazz);
		repository.replaceChildren(list, parentId);
	}
	
	/**
	 * Creates the SQL statement to create the table according columns definitions.
	 * 
	 * @return SQL statement.
	 */
	public String getCreateTableSQL() {
		StringBuilder builder = new StringBuilder();
		// Add columns
		for (Column column : getColumns()) {
			addColumn(builder, column);
			builder.append(", ");
		}
		// Add references
		StringBuilder referencesBuilder = new StringBuilder();
		for (Column column : getColumns()) {
			if (column.getReference() != null) {
				referencesBuilder.append("FOREIGN KEY(").append(column.getColumnName()).append(") REFERENCES ").append(
					column.getReference().getTableName()).append("(").append(
					column.getReference().getColumn().getColumnName()).append(") ON DELETE CASCADE, ");
			}
		}
		// Add unique constraint
		StringBuilder uniqueBuilder = new StringBuilder();
		boolean first = true;
		for (Column column : getColumns()) {
			if (column.isUnique()) {
				if (!first) {
					uniqueBuilder.append(", ");
				}
				first = false;
				uniqueBuilder.append(column.getColumnName());
			}
		}
		return getCreateTableSQL(builder.toString(), referencesBuilder.toString(), uniqueBuilder.toString());
	}
	
	/**
	 * Creates the SQL statement to create the table using given columns definitions.
	 * 
	 * @param columns columns definitions.
	 * @param references reference constraints.
	 * @param uniqueColumns unique constraints.
	 * @return SQL statement.
	 */
	private String getCreateTableSQL(String columns, String references, String uniqueColumns) {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE ").append(getTableName()).append("(");
		builder.append(columns);
		builder.append(references);
		builder.append("UNIQUE (").append(uniqueColumns).append(") ON CONFLICT REPLACE");
		builder.append(");");
		return builder.toString();
	}
	
	/**
	 * Generate the SQL definition for a column.
	 * 
	 * @param builder current StringBuilder to add the SQL definition.
	 * @param column column definition.
	 */
	private void addColumn(StringBuilder builder, Column column) {
		builder.append(column.getColumnName());
		builder.append(" ");
		builder.append(column.getDataType().getType());
		Boolean optional = column.isOptional();
		if (optional != null) {
			builder.append(optional ? " NULL " : " NOT NULL ");
		}
		String extraQualifier = column.getExtraQualifier();
		if (extraQualifier != null) {
			builder.append(" ");
			builder.append(extraQualifier);
			builder.append(" ");
		}
	}
	
	/**
	 * Returns a list of statements to upgrade the SQL scheme.
	 * 
	 * @return a list of statements.
	 */
	public String[] getDefaultUpgradeSQL() {
		return new String[] { getCreateTableSQL() };
	}
	
	/**
	 * Returns the default projection which includes all the columns defined by {@link #getColumns()}
	 * 
	 * @return the projection.
	 */
	protected String[] getProjection() {
		String[] projection = new String[getColumns().length];
		for (int i = 0; i < projection.length; i++) {
			projection[i] = getColumns()[i].getColumnName();
		}
		return projection;
	}
}
