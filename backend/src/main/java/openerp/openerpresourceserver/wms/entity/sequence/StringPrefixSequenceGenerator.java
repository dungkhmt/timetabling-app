package openerp.openerpresourceserver.wms.entity.sequence;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * StringPrefixSequenceGenerator là một lớp mở rộng của SequenceStyleGenerator nhằm tạo ra khóa chính tự động
 * với tiền tố (prefix) và định dạng số tùy chỉnh.
 */
public class StringPrefixSequenceGenerator extends SequenceStyleGenerator {
    // Các hằng số để lấy tham số từ cấu hình
    public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
    public static final String VALUE_PREFIX_DEFAULT = "";
    private String valuePrefix;

    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
    public static final String NUMBER_FORMAT_DEFAULT = "%d";
    private String numberFormat;

    public static final String SEQUENCE_TABLE_PARAMETER = "sequenceTable";
    public static final String SEQUENCE_TABLE_DEFAULT = "";
    private String sequenceTable;

    /**
     * Cấu hình trình tạo chuỗi với các tham số từ Hibernate.
     *
     * @param type            loại dữ liệu của khóa chính.
     * @param params          các tham số cấu hình.
     * @param serviceRegistry service registry của Hibernate.
     * @throws MappingException nếu có lỗi trong quá trình ánh xạ.
     */
    @Override
    public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(type, params, serviceRegistry);
        // Lấy giá trị từ cấu hình hoặc sử dụng giá trị mặc định
        valuePrefix = ConfigurationHelper.getString(VALUE_PREFIX_PARAMETER, params, VALUE_PREFIX_DEFAULT);
        numberFormat = ConfigurationHelper.getString(NUMBER_FORMAT_PARAMETER, params, NUMBER_FORMAT_DEFAULT);
        sequenceTable = ConfigurationHelper.getString(SEQUENCE_TABLE_PARAMETER, params, SEQUENCE_TABLE_DEFAULT);
    }

    /**
     * Sinh ra một khóa chính với tiền tố và định dạng số.
     *
     * @param session phiên hiện tại của Hibernate.
     * @param object  đối tượng đang được lưu.
     * @return khóa chính dưới dạng chuỗi với tiền tố.
     * @throws HibernateException nếu có lỗi trong quá trình sinh khóa chính.
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        String query = "INSERT INTO " + sequenceTable + " VALUES (NULL)";
        try (Statement statement = session.getJdbcConnectionAccess().obtainConnection().createStatement()) {
            // Thực thi truy vấn chèn vào bảng sequence để sinh giá trị tự động
            statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

//            try {
//                // Kiểm tra xem đối tượng đã có ID hay chưa
//                Serializable id = (Serializable) object.getClass().getMethod("getSubId").invoke(object);
//
//                // Nếu đối tượng đã có ID, không tạo mới mà trả về ID hiện tại
//                if (id != null && !id.toString().trim().isEmpty()) { // Thêm trim() để loại bỏ khoảng trắng
//                    return id;
//                }
//            } catch (Exception e) {
//                throw new HibernateException("Không thể lấy ID từ đối tượng", e);
//            }

            // Lấy giá trị của khóa tự sinh
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                long seqValue = rs.getLong(1);
                // Trả về khóa chính với tiền tố và định dạng số
                return valuePrefix + String.format(numberFormat, seqValue);
            }
        } catch (SQLException e) {
            throw new HibernateException("Không thể sinh chuỗi", e);
        }
        return null;
    }
}
