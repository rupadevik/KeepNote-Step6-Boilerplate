package com.stackroute.keepnote.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stackroute.keepnote.exception.NoteNotFoundExeption;
import com.stackroute.keepnote.model.Note;
import com.stackroute.keepnote.model.NoteUser;
import com.stackroute.keepnote.repository.NoteRepository;

/*
* Service classes are used here to implement additional business logic/validation 
* This class has to be annotated with @Service annotation.
* @Service - It is a specialization of the component annotation. It doesn't currently 
* provide any additional behavior over the @Component annotation, but it's a good idea 
* to use @Service over @Component in service-layer classes because it specifies intent 
* better. Additionally, tool support and additional behavior might rely on it in the 
* future.
* */
@Service
public class NoteServiceImpl implements NoteService{

	/*
	 * Autowiring should be implemented for the NoteRepository and MongoOperation.
	 * (Use Constructor-based autowiring) Please note that we should not create any
	 * object using the new keyword.
	 */
	
	@Autowired
	private NoteRepository noteRepository;
	
	
	
	public NoteServiceImpl(NoteRepository noteRepository) {
		super();
		this.noteRepository = noteRepository;
	}

	/*
	 * This method should be used to save a new note.
	 */
	

	public boolean createNote(Note note) {
		/*List<Note> noteList = new ArrayList<>();
		noteList.add(note);
		NoteUser noteuser = new NoteUser();
		noteuser.setUserId(note.getNoteCreatedBy());
		noteuser.setNotes(noteList);
		
		NoteUser noteUser =  noteRepository.insert(noteuser);
		
		if(noteUser!=null)
		{
			return true;
		}
			return false;*/
		Random random = new Random();
		int noteId = random.nextInt(99999);
		note.setNoteId(noteId);
		boolean created = false;
		Optional<NoteUser> noteUser = noteRepository.findById(note.getNoteCreatedBy());
		NoteUser userNotes = null;
		if (noteUser.isPresent()) {
			userNotes = noteUser.get();
			userNotes.getNotes().add(note);
			created = noteRepository.save(userNotes) != null ? true : false;
		} else {
			userNotes = new NoteUser();
			userNotes.setUserId(note.getNoteCreatedBy());
			userNotes.setNotes(Arrays.asList(note));
			created = noteRepository.insert(userNotes) != null ? true : false;
		}

		return created;
	}
	/* This method should be used to delete an existing note. */

	
	public boolean deleteNote(String userId, int noteId) {
		
		boolean delete = false;
		NoteUser noteUser = null;
		try {
			noteUser = noteRepository.findById(userId).get();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		}
		if (noteUser != null) {
			Note noteNew = noteUser.getNotes().stream().filter(note -> note.getNoteId() == noteId).findFirst()
					.orElse(null);
			noteUser.getNotes().remove(noteNew);
			noteRepository.save(noteUser);
			delete = true;
		}
		return delete;
	}
		
		
	
	/* This method should be used to delete all notes with specific userId. */

	
	public boolean deleteAllNotes(String userId) {
		Optional<NoteUser> noteUserOptional = noteRepository.findById(userId);
		NoteUser noteUser = noteUserOptional.get();
		
		if(noteUser.getNotes()!=null)
		{
			List<Note> filteredNotes = new ArrayList<>();
			List<Note>   notes = noteUser.getNotes();
			notes.forEach(note-> {
				if(!note.getNoteCreatedBy().equals(userId))
				{
					filteredNotes.add(note);
				}
			});
			noteUser.setNotes(filteredNotes);
			noteRepository.save(noteUser);
			return true;
		}
		return false;
	}

	/*
	 * This method should be used to update a existing note.
	 */
	public Note updateNote(Note note, int id, String userId) throws NoteNotFoundExeption {
		
		try
		{
		Optional<NoteUser> noteUserOptional = noteRepository.findById(userId);
		NoteUser noteUser = noteUserOptional.get();
		if (noteUser.getNotes() != null) {
			List<Note> notes = noteUser.getNotes();
			List<Note> updateNotesList = new ArrayList<>();
			for (Note noteIter : notes) {
				if (noteIter.getNoteId() == id) {
					updateNotesList.add(note);
				} else {
					updateNotesList.add(noteIter);
				}
			}
			noteUser.setNotes(updateNotesList);
			noteRepository.save(noteUser);
		}
		}
		catch(NoSuchElementException exception)
		{
			throw new NoteNotFoundExeption("NoteNotFoundExeption");
		}
		return note;
	}

	/*
	 * This method should be used to get a note by noteId created by specific user
	 */
	public Note getNoteByNoteId(String userId, int noteId) throws NoteNotFoundExeption {
		
		Note noteReturn = null;
		try
		{
		Optional<NoteUser> noteUserOptional = noteRepository.findById(userId);
		NoteUser noteUser = noteUserOptional.get();
		if (noteUser.getNotes() != null) {
			List<Note> notes = noteUser.getNotes();

			for (Note note : notes) {
				if (note.getNoteId() == noteId) {
					noteReturn = note;
				}
			}
		}
		}
		catch(NoSuchElementException exception)
		{
			throw new NoteNotFoundExeption("NoteNotFoundExeption");
		}
		return noteReturn;
	}

	/*
	 * This method should be used to get all notes with specific userId.
	 */
	public List<Note> getAllNoteByUserId(String userId) {
		try {
			NoteUser noteUser = noteRepository.findById(userId).get();
			return noteUser.getNotes();
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			return new ArrayList<>();
		}
	}

}