package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import java.util.List;

/**
 * FileLocationRepository is the repository class for managing File Locations
 * Most of the methods in this class are being used by FileLocationService.
 *
 * @author TEAM: James Turnbull, SinMan Chan(Cindy), Harpreet Jhita, Mehmet Tahir Dede
 */
public interface FileLocationRepository extends JpaRepository<FileLocation, Integer> {

    /**
     * Returns a list of file locations by tribunal office
     * from table et_cos.file_location.
     *
     * @param tribunalOffice tribunal office
     * @return a list of file locations
     */
    List<FileLocation> findByTribunalOffice(TribunalOffice tribunalOffice);

    /**
     * Returns a list of file locations by tribunal office in ascending
     * order of file location names from table et_cos.file_location
     *
     * @param tribunalOffice tribunal office
     * @return a list of file locations
     */
    List<FileLocation> findByTribunalOfficeOrderByNameAsc(TribunalOffice tribunalOffice);

    /**
     * Returns only one file location by file location code and
     * tribunal office from table et_cos.file_location
     *
     * @param fileLocationCode file location code,
     * @param tribunalOffice tribunal office
     * @return file location
     */
    FileLocation findByCodeAndTribunalOffice(String fileLocationCode, TribunalOffice tribunalOffice);

    /**
     * Returns a long value which is the
     * number of deleted items from table et_cos.file_location.
     * Unsuccessful case returns -1
     * Usage: <a href="https://www.baeldung.com/spring-data-jpa-deleteby"> deleteBy </a>
     *
     * @param tribunalOffice tribunal office
     * @return number of deleted items
     */
    long deleteByTribunalOffice(TribunalOffice tribunalOffice);

    /**
     * Returns true if any data exists in et_cos.file_location table
     * for the parameters file location code and tribunal office and false if not.
     * Usage: <a href="https://www.baeldung.com/spring-data-exists-query"> existsBy </a>
     *
     * @param code file location code
     * @param tribunalOffice tribunal office
     * @return number of deleted items
     */
    boolean existsByCodeAndTribunalOffice(String code, TribunalOffice tribunalOffice);

    /**
     * Returns true if any data exists in et_cos.file_location table
     * for the parameters file location name and tribunal office and false if not.
     * Usage: <a href="https://www.baeldung.com/spring-data-exists-query"> existsBy </a>
     *
     * @param name file location name
     * @param tribunalOffice tribunal office
     * @return number of deleted items
     */
    boolean existsByNameAndTribunalOffice(String name, TribunalOffice tribunalOffice);
}
