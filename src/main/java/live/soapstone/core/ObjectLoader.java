package live.soapstone.core;

import live.soapstone.core.entity.Model;
import live.soapstone.core.utils.Utils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ObjectLoader {

    private List<Integer> vaos = new ArrayList<>();
    private List<Integer> vbos = new ArrayList<>();
    private List<Integer> textures = new ArrayList<>();

    public Model loadOBJModel(String fileName){
        List<String> lines = Utils.readAllLines(fileName);

        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Vector3i> faces = new ArrayList<>();

        for(String line : lines){
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] tokens = line.split("\\s+");

            switch (tokens[0]){
                case "v":
                    //vertices
                    Vector3f verticesVec = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    vertices.add(verticesVec);
                    break;
                case "vt":
                    //vertex textures
                    Vector2f textureVec = new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2])
                    );
                    textures.add(textureVec);
                    break;
                case "vn":
                    //vertex normals
                    Vector3f normalsVec = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    );
                    normals.add(normalsVec);
                    break;
                case "f":
                    //faces
                    List<Vector3i> faceVertices = new ArrayList<>();
                    for (int tokenIndex = 1; tokenIndex < tokens.length; tokenIndex++) {
                        faceVertices.add(parseFaceVertex(tokens[tokenIndex]));
                    }

                    for (int vertexIndex = 1; vertexIndex < faceVertices.size() - 1; vertexIndex++) {
                        faces.add(faceVertices.get(0));
                        faces.add(faceVertices.get(vertexIndex));
                        faces.add(faceVertices.get(vertexIndex + 1));
                    }
                    break;
                default:
                    break;
            }
        }

        List<Float> verticesData = new ArrayList<>();
        List<Float> texCoordData = new ArrayList<>();
        List<Float> normalData = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<VertexKey, Integer> uniqueVertices = new HashMap<>();

        for (Vector3i face : faces){
            processVertex(face.x, face.y, face.z, vertices, textures, normals, uniqueVertices, indices, verticesData, texCoordData, normalData);
        }

        float[] verticesArr = toFloatArray(verticesData);
        float[] texCoordArr = toFloatArray(texCoordData);
        float[] normalArr = toFloatArray(normalData);
        int[] indicesArr = indices.stream().mapToInt((Integer v) -> v).toArray();

        return loadModel(verticesArr, texCoordArr, normalArr, indicesArr);
    }

    private static void processVertex(int pos, int texCoord, int normal, List<Vector3f> vertexList,
            List<Vector2f> texCoordList, List<Vector3f> normalList, Map<VertexKey, Integer> vertexMap,
            List<Integer> indicesList, List<Float> verticesData, List<Float> texCoordData, List<Float> normalData) {

        VertexKey vertexKey = new VertexKey(pos, texCoord, normal);
        Integer index = vertexMap.get(vertexKey);
        if (index == null) {
            index = vertexMap.size();
            vertexMap.put(vertexKey, index);

            Vector3f positionVec = vertexList.get(pos);
            verticesData.add(positionVec.x);
            verticesData.add(positionVec.y);
            verticesData.add(positionVec.z);

            if (texCoord >= 0) {
                Vector2f texCoordVec = texCoordList.get(texCoord);
                texCoordData.add(texCoordVec.x);
                texCoordData.add(1 - texCoordVec.y);
            } else {
                texCoordData.add(0f);
                texCoordData.add(0f);
            }

            if (normal >= 0) {
                Vector3f normalVec = normalList.get(normal);
                normalData.add(normalVec.x);
                normalData.add(normalVec.y);
                normalData.add(normalVec.z);
            } else {
                normalData.add(0f);
                normalData.add(0f);
                normalData.add(0f);
            }
        }

        indicesList.add(index);
    }

    private static float[] toFloatArray(List<Float> data) {
        float[] result = new float[data.size()];
        for (int i = 0; i < data.size(); i++) {
            result[i] = data.get(i);
        }
        return result;
    }

    private static final class VertexKey {
        private final int positionIndex;
        private final int textureIndex;
        private final int normalIndex;

        private VertexKey(int positionIndex, int textureIndex, int normalIndex) {
            this.positionIndex = positionIndex;
            this.textureIndex = textureIndex;
            this.normalIndex = normalIndex;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof VertexKey)) {
                return false;
            }
            VertexKey that = (VertexKey) other;
            return positionIndex == that.positionIndex
                    && textureIndex == that.textureIndex
                    && normalIndex == that.normalIndex;
        }

        @Override
        public int hashCode() {
            int result = Integer.hashCode(positionIndex);
            result = 31 * result + Integer.hashCode(textureIndex);
            result = 31 * result + Integer.hashCode(normalIndex);
            return result;
        }
    }

    private static Vector3i parseFaceVertex(String token) {
        String[] lineToken = token.split("/");
        int length = lineToken.length;
        int pos = -1, coords = -1, normal = -1;
        pos = Integer.parseInt(lineToken[0]) - 1;
        if (length > 1) {
            String textCoord = lineToken[1];
            coords = textCoord.length() > 0 ? Integer.parseInt(textCoord) - 1 : -1;

            if (length > 2) {
                normal = Integer.parseInt(lineToken[2]) - 1;
            }
        }
        return new Vector3i(pos, coords, normal);
    }


    public Model loadModel(float[] vertices, float[] textureCoords, float[] normals, int[] indices) {
        int id = createVAO();
        storeIndicesBuffer(indices);
        storeDataInAttribList(0, 3, vertices);
        storeDataInAttribList(1, 2, textureCoords);
        storeDataInAttribList(2, 3, normals);
        unbind();
        return new Model(id, indices.length);
    }

    public int loadTexture(String filename) throws Exception {
        int width, height;
        ByteBuffer buffer;
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);

            buffer = STBImage.stbi_load(filename, w, h, c, 4);
            if(buffer == null) {
                throw new Exception("Image file " + filename + " not loaded " + STBImage.stbi_failure_reason());
            }

            width = w.get();
            height = h.get();
        }

        int id = GL11.glGenTextures();
        textures.add(id);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        STBImage.stbi_image_free(buffer);
        return id;
    }

    private int createVAO(){
        int id = GL30.glGenVertexArrays();
        vaos.add(id);
        GL30.glBindVertexArray(id);
        return id;
    }

    private void storeIndicesBuffer(int[] indices){
        int vbo = GL15.glGenBuffers();
        vbos.add(vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
        IntBuffer buffer = Utils.storeDataInIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
    }

    private void storeDataInAttribList(int attribNo, int vertexCount, float[] data) {
        int vbo = GL15.glGenBuffers();
        vbos.add(vbo);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = Utils.storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attribNo, vertexCount, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void cleanup() {
        for(int vao : vaos){
            GL30.glDeleteVertexArrays(vao);
        }
        for(int vbo : vbos){
            GL30.glDeleteBuffers(vbo);
        }
        for(int texture : textures){
            GL11.glDeleteTextures(texture);
        }
    }

}
